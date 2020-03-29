package com.brentcroft.tools.jstl.tag;


import lombok.Getter;

/**
 * (see http://docs.oracle.com/javaee/6/api/javax/servlet/jsp/jstl/core/
 * LoopTagStatus.html)
 *
 * @param <T>
 * @author ADobson
 */
@Getter
public class LoopTagStatus< T >
{
    private final Integer begin;
    private final Integer end;
    private final Integer step;
    private int index = 0;
    private T current;

    public LoopTagStatus( Integer begin, Integer end, Integer step )
    {
        this.begin = begin;
        this.end = end;
        this.step = step;

        //
        if ( begin != null )
        {
            index = begin;
        }
    }

    public LoopTagStatus< T > withCurrent( T current )
    {
        this.current = current;
        return this;
    }


    public void increment()
    {
        index++;
    }

    public void increment( int step )
    {
        index = index + step;
    }

    public int getCount()
    {
        return index + 1;
    }

    /**
     * Flag indicating whether the current round is the first pass through the
     * iteration.
     *
     * @return true if the current iteration is the first
     */
    public boolean isFirst()
    {
        return begin == null ? index == 0 : ( index <= begin );
    }

    /**
     * Flag indicating whether the current round is the last pass through the
     * iteration (or null if we don't know).
     *
     * @return null or boolean is last pass of iteration
     */
    public Boolean isLast()
    {
        return end == null ? null : ( index >= end );
    }


    public void setIndex( Integer begin )
    {
        index = ( begin == null ? 0 : begin );
    }
}