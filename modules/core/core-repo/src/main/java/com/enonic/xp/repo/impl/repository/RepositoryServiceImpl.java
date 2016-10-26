package com.enonic.xp.repo.impl.repository;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import com.enonic.xp.branch.Branch;
import com.enonic.xp.branch.Branches;
import com.enonic.xp.context.Context;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodeNotFoundException;
import com.enonic.xp.repo.impl.InternalContext;
import com.enonic.xp.repo.impl.index.IndexServiceInternal;
import com.enonic.xp.repo.impl.storage.NodeStorageService;
import com.enonic.xp.repository.CreateBranchParams;
import com.enonic.xp.repository.CreateRepositoryParams;
import com.enonic.xp.repository.DeleteRepositoryParams;
import com.enonic.xp.repository.NodeRepositoryService;
import com.enonic.xp.repository.Repositories;
import com.enonic.xp.repository.Repository;
import com.enonic.xp.repository.RepositoryConstants;
import com.enonic.xp.repository.RepositoryId;
import com.enonic.xp.repository.RepositoryNotFoundException;
import com.enonic.xp.repository.RepositoryService;

@Component(immediate = true)
public class RepositoryServiceImpl
    implements RepositoryService
{
    private static final Logger LOG = LoggerFactory.getLogger( RepositoryServiceImpl.class );

    private final ConcurrentMap<RepositoryId, Repository> repositoryMap = Maps.newConcurrentMap();

    private RepositoryEntryService repositoryEntryService;

    private IndexServiceInternal indexServiceInternal;

    private NodeRepositoryService nodeRepositoryService;

    private NodeStorageService nodeStorageService;

    @SuppressWarnings("unused")
    @Activate
    public void initialize()
    {
        if ( this.indexServiceInternal.isMaster() )
        {
            new SystemRepoInitializer( this ).initialize();
        }
    }

    @Override
    public Repository createRepository( final CreateRepositoryParams params )
    {
        return repositoryMap.compute( params.getRepositoryId(), ( key, previousRepository ) -> {
            if ( previousRepository != null || repositoryEntryService.getRepositoryEntry( key ) != null )
            {
                throw new RepositoryAlreadyExistException( key );
            }
            createRootNode( params, RepositoryConstants.MASTER_BRANCH );
            final Repository repository = createRepositoryObject( params );
            repositoryEntryService.createRepositoryEntry( repository );
            return repository;
        } );
    }

    @Override
    public Branch createBranch( final CreateBranchParams createBranchParams )
    {
        final RepositoryId repositoryId = ContextAccessor.current().
            getRepositoryId();

        repositoryMap.compute( repositoryId, ( key, previousRepository ) -> {
            previousRepository = previousRepository == null ? repositoryEntryService.getRepositoryEntry( key ) : previousRepository;
            if ( previousRepository == null )
            {
                throw new RepositoryNotFoundException( "Cannot create branch in repository [" + repositoryId + "], not found" );
            }

            final Branch newBranch = createBranchParams.getBranch();
            if ( previousRepository.getBranches().contains( newBranch ) )
            {
                throw new BranchAlreadyExistException( newBranch );
            }

            pushRootNode( previousRepository, newBranch );
            final Repository newRepository = repositoryEntryService.addBranchToRepositoryEntry( repositoryId, newBranch );
            return newRepository;
        } );

        return createBranchParams.getBranch();
    }

    @Override
    public Repositories list()
    {
        final ImmutableList.Builder<Repository> repositories = ImmutableList.builder();
        repositoryEntryService.
            findRepositoryEntryIds().
            stream().
            map( repositoryId -> repositoryEntryService.getRepositoryEntry( repositoryId ) ).
            filter( Objects::nonNull ).
            forEach( repositories::add );
        return Repositories.from( repositories.build() );
    }

    @Override
    public boolean isInitialized( final RepositoryId repositoryId )
    {
        return this.get( repositoryId ) != null;
    }

    @Override
    public Repository get( final RepositoryId repositoryId )
    {
        return repositoryMap.computeIfAbsent( repositoryId, key -> repositoryEntryService.getRepositoryEntry( repositoryId ) );
    }

    @Override
    public RepositoryId deleteRepository( final DeleteRepositoryParams params )
    {
        final RepositoryId repositoryId = params.getRepositoryId();
        repositoryMap.compute( repositoryId, ( key, previousRepository ) -> {
            repositoryEntryService.deleteRepositoryEntry( repositoryId );
            nodeRepositoryService.delete( repositoryId );
            return null;
        } );
        return repositoryId;
    }

    private Repository createRepositoryObject( final CreateRepositoryParams params )
    {
        return Repository.create().
            id( params.getRepositoryId() ).
            branches( Branches.from( RepositoryConstants.MASTER_BRANCH ) ).
            settings( params.getRepositorySettings() ).
            build();
    }

    private void createRootNode( final CreateRepositoryParams params, final Branch branch )
    {
        if ( !this.nodeRepositoryService.isInitialized( params.getRepositoryId() ) )
        {
            this.nodeRepositoryService.create( params );
        }

        final InternalContext rootNodeContext = InternalContext.create( ContextAccessor.current() ).
            repositoryId( params.getRepositoryId() ).
            branch( branch ).
            build();

        final Node rootNode = this.nodeStorageService.store( Node.createRoot().
            permissions( params.getRootPermissions() ).
            inheritPermissions( params.isInheritPermissions() ).
            childOrder( params.getRootChildOrder() ).
            build(), rootNodeContext );

        LOG.info( "Created root node in  with id [" + rootNode.id() + "] in repository [" + params.getRepositoryId() + "]" );
    }

    private Branch pushRootNode( final Repository currentRepo, final Branch branch )
    {
        final Context context = ContextAccessor.current();
        final InternalContext internalContext = InternalContext.create( context ).branch( RepositoryConstants.MASTER_BRANCH ).build();
        final Node rootNode = this.nodeStorageService.get( Node.ROOT_UUID, internalContext );

        if ( rootNode == null )
        {
            throw new NodeNotFoundException( "Cannot find root-node in repository [" + currentRepo + "]" );
        }

        this.nodeStorageService.push( rootNode, branch, internalContext );

        return branch;
    }

    @Reference
    public void setRepositoryEntryService( final RepositoryEntryService repositoryEntryService )
    {
        this.repositoryEntryService = repositoryEntryService;
    }

    @Reference
    public void setIndexServiceInternal( final IndexServiceInternal indexServiceInternal )
    {
        this.indexServiceInternal = indexServiceInternal;
    }

    @Reference
    public void setNodeRepositoryService( final NodeRepositoryService nodeRepositoryService )
    {
        this.nodeRepositoryService = nodeRepositoryService;
    }

    @Reference
    public void setNodeStorageService( final NodeStorageService nodeStorageService )
    {
        this.nodeStorageService = nodeStorageService;
    }
}
