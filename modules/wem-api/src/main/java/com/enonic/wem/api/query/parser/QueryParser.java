package com.enonic.wem.api.query.parser;

import java.util.List;

import org.codehaus.jparsec.error.ParserException;

import com.google.common.base.Strings;

import com.enonic.wem.api.query.QueryException;
import com.enonic.wem.api.query.expr.ConstraintExpr;
import com.enonic.wem.api.query.expr.OrderExpr;
import com.enonic.wem.api.query.expr.QueryExpr;

public final class QueryParser
{
    private final QueryGrammar grammar;

    public QueryParser()
    {
        this.grammar = new QueryGrammar();
    }

    private QueryExpr doParse( final String query )
    {
        try
        {
            return this.grammar.grammar().parse( query );
        }
        catch ( final ParserException e )
        {
            throw new QueryException( e.getMessage() );
        }
    }

    private List<OrderExpr> doParseOrderBy( final String orderExpressions )
    {
        try
        {
            return this.grammar.orderExpressionsGrammar().parse( orderExpressions );
        }
        catch ( final ParserException e )
        {
            throw new QueryException( e.getMessage() );
        }
    }

    private ConstraintExpr doParseConstraint( final String contstraintsExpression )
    {
        try
        {
            return this.grammar.constraintExpressionsGrammar().parse( contstraintsExpression );
        }
        catch ( final ParserException e )
        {
            throw new QueryException( e.getMessage() );
        }
    }

    public static QueryExpr parse( final String query )
    {
        return new QueryParser().doParse( query );
    }

    public static List<OrderExpr> parseOrderExpressions( final String orderExpressions )
    {
        return new QueryParser().doParseOrderBy( orderExpressions );
    }

    public static ConstraintExpr parseCostraintExpression( final String constraints )
    {
        if ( Strings.isNullOrEmpty( constraints ) )
        {
            return null;
        }
        return new QueryParser().doParseConstraint( constraints );
    }
}
