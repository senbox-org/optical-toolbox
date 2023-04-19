/*
 * $Id: Interp.java,v 1.1 2007/03/27 12:51:06 marcoz Exp $
 *
 * Copyright (c) 2003 Brockmann Consult GmbH. All right reserved.
 * http://www.brockmann-consult.de
 */
package eu.esa.opt.util.math;

// TODO make this public API

/**
 * <p><i><b>IMPORTANT NOTE:</b>
 * This class belongs to a preliminary API.
 * It is not (yet) intended to be used by clients and may change in the future.</i></p>
 */
public class Interp {

    /**
     * The method returns a status code which can be one of the following values: <blockquote> 0 : Normal <br> 1 :
     * ascending xi && x >= xi[ni-1] <br> 2 : ascending xi  && x <= xi[0]  <br> 3 : descending xi  && x >= xi[0]  <br> 4
     * : descending xi  && x <= xi[ni-1]  <br> 5 : i for x not found in xi <br> 6 : i for x not found in xi <br>
     * </blockquote>
     * <p/>
     * <p><i>Notes: interpolation computations are all done in <code>double</code>, no array bounds checking on i, p (assumed to have
     * nd valid elements)<br> Reference: Numerical Recipies in C, Page 117.</i> </p>
     *
     * @param x  co-ordinate of the point to interpolate
     * @param xi tabulated values to search in
     * @param i  the fractional index, must not be <code>null</code>, contains the result
     * @return the status code, see above
     */
    public static int interpCoord(final double x, final double[] xi, final FractIndex i) {
        final int n = xi.length;
        final boolean ascending = xi[n - 1] >= xi[0]; /* investigating ascending/descending */

        i.fraction = 0.0; /* out of range default */
        int status = 0;
        if (ascending && x >= xi[n - 1]) {
            i.index = n - 1;
            if (x > xi[n - 1]) {
                // todo - chk: why not compute fraction (> 1) here for extrapolation
                // todo - log warning here
                status = 1;
            }
        } else if (ascending && x <= xi[0]) {
            i.index = 0;
            if (x < xi[0]) {
                // todo - chk: why not compute fraction (< 0) here for extrapolation?
                // todo - log warning here
                status = 2;
            }
        } else if (!ascending && x >= xi[0]) {
            i.index = 0;
            if (x > xi[0]) {
                // todo - chk: why not compute fraction (> 1) here for extrapolation?
                // todo - log warning here
                status = 3;
            }
        } else if (!ascending && x <= xi[n - 1]) {
            i.index = n - 1;
            if (x < xi[n - 1]) {
                // todo - chk: why not compute fraction (< 0) here for extrapolation?
                // todo - log warning here
                status = 4;
            }
        } else {
            int il = -1;  /* Initialize lower limits */
            int iu = n;  /* Initialize upper limits */
            int im;
            while (iu - il > 1) {
                im = (iu + il) >> 1;
                if (x >= xi[im] == ascending) {
                    il = im;
                } else {
                    iu = im;
                }
            }
            if (il == -1) {
                // todo - chk: throw IllegalArgumentException because this situation occurs only
                // if the xi are not ordered (neither ascending nor descending)
                // todo - log error here
                i.index = 0;
                status = 5;
            } else if (il == n) {
                // todo - chk: throw IllegalArgumentException because this situation occurs only
                // if the xi are not ordered (neither ascending nor descending)
                // todo - log error here
                i.index = n - 1;
                status = 6;
            } else if (ascending && x == xi[il]) {
                i.index = il;
            } else if (!ascending && x == xi[il + 1]) {
                i.index = il + 1;
            } else {
                i.index = il;
                i.fraction = (x - xi[il]) / (xi[il + 1] - xi[il]);
            }
        }
        return status;
    }

    /**
     * Multi-linear interpolation in arrays of any dimension.
     *
     * @param elements a multi-dimensional array of primitive type <code>float[sizes[0]][sizes[1]]...[sizes[nd-1]]</code>
     * @param indexes  the fractional indexes for each dimension
     */
    public static double interpolate(final Object elements, final FractIndex[] indexes) {
        return interpolate(elements, indexes, 0);
    }

    /**
     * Multi-linear interpolation in arrays of any dimension.
     *
     * @param elements a multi-dimensional array of primitive type <code>float</code> or <code>double</code>, with the
     *                 array dimensions <code>[sizes[0]][sizes[1]]...[sizes[rank-1]]</code>, where <code>rank</code> is
     *                 the number of dimensions
     * @param indexes  the fractional indexes for each dimension, the length of this array must not be less than the
     *                 number of array dimensions
     * @param dim      current dimension index, must be greater or equal zero and less than the length of
     *                 <code>indexes</code>
     */
    public static double interpolate(final Object elements, final FractIndex[] indexes, final int dim) {
        final int rank = indexes.length;
        final int index = indexes[dim].index;
        final double fraction = indexes[dim].fraction;
        if (fraction < 0.0 || fraction >= 1.0) {
            throw new IllegalArgumentException("fraction < 0.0 || fraction >= 1.0, fraction=" + fraction);
        }
        final double y1;
        final double y2;
        if (dim == rank - 1) {
            if (elements instanceof float[]) {
                final float[] array = (float[]) elements;
                y1 = array[index];
                y2 = (index < array.length - 1) ? array[index + 1] : y1;
            } else if (elements instanceof double[]) {
                final double[] array = (double[]) elements;
                y1 = array[index];
                y2 = (index < array.length - 1) ? array[index + 1] : y1;
            } else {
                throw new IllegalArgumentException("illegal array type, float[] or double[] expected");
            }
        } else if (dim >= 0 && dim < rank - 1) {
            final Object[] array = (Object[]) elements;
            y1 = interpolate(array[index], indexes, dim + 1);
            y2 = (index < array.length - 1) ? interpolate(array[index + 1], indexes, dim + 1) : y1;
        } else {
            throw new IndexOutOfBoundsException("dim < 0 || dim >= rank, dim=" + index + ", rank=" + rank);
        }
        return y1 + fraction * (y2 - y1);
    }
}
