package com.enonic.xp.repo.impl.repository;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;

import com.enonic.xp.branch.Branch;
import com.enonic.xp.branch.Branches;
import com.enonic.xp.context.Context;
import com.enonic.xp.context.ContextAccessor;
import com.enonic.xp.context.ContextBuilder;
import com.enonic.xp.exception.ForbiddenAccessException;
import com.enonic.xp.node.AttachedBinary;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodeNotFoundException;
import com.enonic.xp.node.RefreshMode;
import com.enonic.xp.repo.impl.InternalContext;
import com.enonic.xp.repo.impl.index.IndexServiceInternal;
import com.enonic.xp.repo.impl.node.DeleteNodeByIdCommand;
import com.enonic.xp.repo.impl.node.RefreshCommand;
import com.enonic.xp.repo.impl.search.NodeSearchService;
import com.enonic.xp.repo.impl.storage.NodeStorageService;
import com.enonic.xp.repository.BranchNotFoundException;
import com.enonic.xp.repository.CreateBranchParams;
import com.enonic.xp.repository.CreateRepositoryParams;
import com.enonic.xp.repository.DeleteBranchParams;
import com.enonic.xp.repository.DeleteRepositoryParams;
import com.enonic.xp.repository.EditableRepository;
import com.enonic.xp.repository.NodeRepositoryService;
import com.enonic.xp.repository.Repositories;
import com.enonic.xp.repository.Repository;
import com.enonic.xp.repository.RepositoryConstants;
import com.enonic.xp.repository.RepositoryId;
import com.enonic.xp.repository.RepositoryNotFoundException;
import com.enonic.xp.repository.RepositoryService;
import com.enonic.xp.repository.UpdateRepositoryParams;
import com.enonic.xp.security.RoleKeys;
import com.enonic.xp.security.auth.AuthenticationInfo;
import com.enonic.xp.util.BinaryReference;

public class RepositoryServiceImpl
    implements RepositoryService
{
    private static final Logger LOG = LoggerFactory.getLogger( RepositoryServiceImpl.class );

    private final ConcurrentMap<RepositoryId, Repository> repositoryMap = new ConcurrentHashMap<>();

    private final RepositoryEntryService repositoryEntryService;

    private final IndexServiceInternal indexServiceInternal;

    private final NodeRepositoryService nodeRepositoryService;

    private final NodeStorageService nodeStorageService;

    private final NodeSearchService nodeSearchService;

    public RepositoryServiceImpl( final RepositoryEntryService repositoryEntryService, final IndexServiceInternal indexServiceInternal,
                                  final NodeRepositoryService nodeRepositoryService, final NodeStorageService nodeStorageService,
                                  final NodeSearchService nodeSearchService )
    {
        this.repositoryEntryService = repositoryEntryService;
        this.indexServiceInternal = indexServiceInternal;
        this.nodeRepositoryService = nodeRepositoryService;
        this.nodeStorageService = nodeStorageService;
        this.nodeSearchService = nodeSearchService;
    }

    public void initialize()
    {
        SystemRepoInitializer.create().
            setIndexServiceInternal( indexServiceInternal ).
            setRepositoryService( this ).
            setNodeStorageService( nodeStorageService ).
            build().
            initialize();
    }

    @Override
    public Repository createRepository( final CreateRepositoryParams params )
    {
        requireAdminRole();

        return repositoryMap.compute( params.getRepositoryId(), ( repositoryId, previousRepository ) -> doCreateRepo( params ) );
    }

    private Repository doCreateRepo( final CreateRepositoryParams params )
    {
        final RepositoryId repositoryId = params.getRepositoryId();
        final boolean repoAlreadyInitialized = this.nodeRepositoryService.isInitialized( repositoryId );

        if ( repoAlreadyInitialized )
        {
            throw new RepositoryAlreadyExistException( repositoryId );
        }

        this.nodeRepositoryService.create( params );

        createRootNode( params );

        final Repository repository = createRepositoryObject( params );
        repositoryEntryService.createRepositoryEntry( repository );
        return repository;
    }

    @Override
    public Repository updateRepository( final UpdateRepositoryParams params )
    {
        requireAdminRole();

        Repository repository = repositoryMap.compute( params.getRepositoryId(),
                                                       ( key, previousRepository ) -> doUpdateRepository( params, previousRepository ) );

        invalidatePathCache();

        return repository;
    }

    private Repository doUpdateRepository( final UpdateRepositoryParams updateRepositoryParams, Repository previousRepository )
    {
        RepositoryId repositoryId = updateRepositoryParams.getRepositoryId();

        previousRepository = previousRepository == null ? repositoryEntryService.getRepositoryEntry( repositoryId ) : previousRepository;

        if ( previousRepository == null )
        {
            throw new RepositoryNotFoundException( repositoryId );
        }

        final EditableRepository editableRepository = new EditableRepository( previousRepository );

        updateRepositoryParams.getEditor().accept( editableRepository );

        UpdateRepositoryEntryParams params = UpdateRepositoryEntryParams.create().
            repositoryId( repositoryId ).
            repositoryData( editableRepository.data ).
            attachments( ImmutableList.copyOf( editableRepository.binaryAttachments ) ).
            build();

        return repositoryEntryService.updateRepositoryEntry( params );
    }

    @Override
    public Branch createBranch( final CreateBranchParams createBranchParams )
    {
        requireAdminRole();
        final RepositoryId repositoryId = ContextAccessor.current().
            getRepositoryId();

        repositoryMap.compute( repositoryId,
                               ( key, previousRepository ) -> doCreateBranch( createBranchParams, repositoryId, previousRepository ) );

        return createBranchParams.getBranch();
    }

    private Repository doCreateBranch( final CreateBranchParams createBranchParams, final RepositoryId repositoryId,
                                       Repository previousRepository )
    {
        //If the repository entry does not exist, throws an exception
        previousRepository = previousRepository == null ? repositoryEntryService.getRepositoryEntry( repositoryId ) : previousRepository;
        if ( previousRepository == null )
        {
            throw new RepositoryNotFoundException( repositoryId );
        }

        //If the branch already exists, throws an exception
        final Branch newBranch = createBranchParams.getBranch();
        if ( previousRepository.getBranches().contains( newBranch ) )
        {
            throw new BranchAlreadyExistException( newBranch );
        }

        //If the root node does not exist, creates it
        if ( getRootNode( previousRepository.getId(), newBranch ) == null )
        {
            pushRootNode( previousRepository, newBranch );

            RefreshCommand.create().indexServiceInternal( this.indexServiceInternal ).refreshMode( RefreshMode.ALL ).build().execute();
        }

        //Updates the repository entry
        return repositoryEntryService.addBranchToRepositoryEntry( repositoryId, newBranch );
    }

    @Override
    public Repositories list()
    {
        requireAdminRole();
        final ImmutableList.Builder<Repository> repositories = ImmutableList.builder();
        repositoryEntryService.findRepositoryEntryIds().stream().
            map( this::doGet ).
            filter( Objects::nonNull ).
            forEach( repositories::add );
        return Repositories.from( repositories.build() );
    }

    @Override
    public boolean isInitialized( final RepositoryId repositoryId )
    {
        requireAdminRole();
        return this.nodeRepositoryService.isInitialized( repositoryId ) &&
            this.repositoryEntryService.getRepositoryEntry( repositoryId ) != null;
    }

    @Override
    public Repository get( final RepositoryId repositoryId )
    {
        requireAdminRole();
        return doGet( repositoryId );
    }

    private Repository doGet( final RepositoryId repositoryId )
    {
        return repositoryMap.computeIfAbsent( repositoryId, key -> repositoryEntryService.getRepositoryEntry( repositoryId ) );
    }

    @Override
    public RepositoryId deleteRepository( final DeleteRepositoryParams params )
    {
        requireAdminRole();
        final RepositoryId repositoryId = params.getRepositoryId();
        repositoryMap.compute( repositoryId, ( key, previousRepository ) -> {
            repositoryEntryService.deleteRepositoryEntry( repositoryId );
            nodeRepositoryService.delete( repositoryId );
            return null;
        } );

        invalidatePathCache();

        return repositoryId;
    }

    private void invalidatePathCache()
    {
        this.nodeStorageService.invalidate();
    }

    @Override
    public Branch deleteBranch( final DeleteBranchParams params )
    {
        requireAdminRole();
        final RepositoryId repositoryId = ContextAccessor.current().
            getRepositoryId();

        repositoryMap.compute( repositoryId, ( key, previousRepository ) -> doDeleteBranch( params, repositoryId, previousRepository ) );

        invalidatePathCache();

        return params.getBranch();
    }

    private Repository doDeleteBranch( final DeleteBranchParams params, final RepositoryId repositoryId, Repository previousRepository )
    {
        //If the repository entry does not exist, throws an exception
        previousRepository = previousRepository == null ? repositoryEntryService.getRepositoryEntry( repositoryId ) : previousRepository;
        if ( previousRepository == null )
        {
            throw new RepositoryNotFoundException( repositoryId );
        }

        //If the branch does not exist, throws an exception
        final Branch branch = params.getBranch();
        if ( !previousRepository.getBranches().contains( branch ) )
        {
            throw new BranchNotFoundException( branch );
        }

        //If the root node exists, deletes it
        if ( getRootNode( previousRepository.getId(), branch ) != null )
        {
            deleteRootNode( branch );
        }

        //Updates the repository entry
        return repositoryEntryService.removeBranchFromRepositoryEntry( repositoryId, branch );
    }

    @Override
    public void invalidateAll()
    {
        repositoryMap.clear();
    }

    @Override
    public void invalidate( final RepositoryId repositoryId )
    {
        repositoryMap.remove( repositoryId );
    }

    @Override
    public ByteSource getBinary( final RepositoryId repositoryId, final BinaryReference binaryReference )
    {
        requireAdminRole();

        Repository repository = repositoryEntryService.getRepositoryEntry( repositoryId );
        if ( repository == null )
        {
            throw new RepositoryNotFoundException( repositoryId );
        }

        final AttachedBinary attachedBinary = repository.getAttachments().getByBinaryReference( binaryReference );
        return attachedBinary == null ? null : repositoryEntryService.getBinary( attachedBinary );
    }

    private void requireAdminRole()
    {
        final AuthenticationInfo authInfo = ContextAccessor.current().getAuthInfo();
        final boolean hasAdminRole = authInfo.hasRole( RoleKeys.ADMIN );
        if ( !hasAdminRole )
        {
            throw new ForbiddenAccessException( authInfo.getUser() );
        }
    }

    private Repository createRepositoryObject( final CreateRepositoryParams params )
    {
        return Repository.create().
            id( params.getRepositoryId() ).
            branches( Branches.from( RepositoryConstants.MASTER_BRANCH ) ).
            settings( params.getRepositorySettings() ).
            data( params.getData() ).
            build();
    }

    private Node getRootNode( final RepositoryId repositoryId, final Branch branch )
    {
        final Context rootNodeContext = ContextBuilder.from( ContextAccessor.current() ).
            repositoryId( repositoryId ).
            branch( branch ).
            build();

        final InternalContext rootNodeInternalContext = InternalContext.create( rootNodeContext ).build();

        return this.nodeStorageService.get( Node.ROOT_UUID, rootNodeInternalContext );
    }

    private void createRootNode( final CreateRepositoryParams params )
    {
        final Context rootNodeContext = ContextBuilder.from( ContextAccessor.current() ).
            repositoryId( params.getRepositoryId() ).
            branch( RepositoryConstants.MASTER_BRANCH ).build();

        final InternalContext rootNodeInternalContext = InternalContext.create( rootNodeContext ).build();

        final Node rootNode = this.nodeStorageService.store( Node.createRoot()
                                                                 .permissions( params.getRootPermissions() )
                                                                 .inheritPermissions( false )
                                                                 .childOrder( params.getRootChildOrder() )
                                                                 .build(), rootNodeInternalContext );

        rootNodeContext.runWith( () -> RefreshCommand.create()
            .indexServiceInternal( this.indexServiceInternal )
            .refreshMode( RefreshMode.ALL )
            .build()
            .execute() );

        LOG.info( "Created root node with id [{}] in repository [{}]", rootNode.id(), params.getRepositoryId() );
    }

    private void pushRootNode( final Repository currentRepo, final Branch branch )
    {
        final Context context = ContextAccessor.current();
        final InternalContext internalContext = InternalContext.create( context ).branch( RepositoryConstants.MASTER_BRANCH ).build();
        final Node rootNode = this.nodeStorageService.get( Node.ROOT_UUID, internalContext );

        if ( rootNode == null )
        {
            throw new NodeNotFoundException( "Cannot find root-node in repository [" + currentRepo + "]" );
        }

        this.nodeStorageService.push( rootNode, branch, internalContext );
    }

    private void deleteRootNode( final Branch branch )
    {
        ContextBuilder.from( ContextAccessor.current() ).
            branch( branch ).
            build().
            runWith( () -> DeleteNodeByIdCommand.create().
                nodeId( Node.ROOT_UUID ).
                storageService( this.nodeStorageService ).
                searchService( this.nodeSearchService ).
                indexServiceInternal( this.indexServiceInternal ).
                allowDeleteRoot( true ).
                build().
                execute() );

        doRefresh();
    }

    private void doRefresh()
    {
        RefreshCommand.create().
            refreshMode( RefreshMode.ALL ).
            indexServiceInternal( this.indexServiceInternal ).
            build().
            execute();
    }
}
