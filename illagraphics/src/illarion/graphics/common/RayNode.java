/*
 * This file is part of the Illarion Graphics Engine.
 *
 * Copyright © 2011 - Illarion e.V.
 *
 * The Illarion Graphics Engine is free software: you can redistribute i and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * The Illarion Graphics Engine is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * the Illarion Graphics Interface. If not, see <http://www.gnu.org/licenses/>.
 */
package illarion.graphics.common;

import illarion.common.util.Location;

import org.apache.log4j.Logger;

/**
 * A ray node is one node on the path of a light to a destination. It knows all
 * its child nodes around.
 * 
 * @author Nop
 * @author Martin Karing
 * @version 2.00
 * @since 2.00
 */
public final class RayNode {
    /**
     * The string builder used to create the string texts in this class.
     */
    private static final StringBuilder BUILDER = new StringBuilder();

    /**
     * The error and debug logger of the client.
     */
    private static final Logger LOGGER = Logger.getLogger(RayNode.class);

    /**
     * The maximum amount of childs a node can get. This is the size of the
     * array that stores the child references.
     */
    private static final int MAX_CHILDS = 8;

    /**
     * The list of children this node has.
     */
    private final RayNode[] children = new RayNode[MAX_CHILDS];

    /**
     * The amount of children that were already added to this node. This is the
     * index of the next child that shall be added in the {@link #children}
     * array.
     */
    private int childrenCount;

    /**
     * The light intensity value of this node.
     */
    private final float intensity;

    /**
     * The level of the node, means how many steps the node is away from the
     * center of the light. The root ray node has the level 0.
     */
    private final int level;

    /**
     * The x coordinate of this node.
     */
    private final int x;

    /**
     * The y coordinate of this node.
     */
    private final int y;

    /**
     * Create a ray node. A node created with this constructor is placed at the
     * origin of the light as the root node.
     * 
     * @param size the length of the rays of the light this node is a part of,
     *            its used to calculate the intensity of the light
     */
    public RayNode(final float size) {
        this(0, 0, 0, size);
    }

    /**
     * Create a ray node at a location. The location is relative to the origin
     * of the light.
     * 
     * @param posX the relative x coordinate to the origin of the light
     * @param posY the relative y coordinate to the origin of the light
     * @param nodeLevel the level of the node, means how many steps the node is
     *            away from the origin of the light or how many nodes are in
     *            front of this ray node. The root ray node is level 0
     * @param size the length of the rays of the light this node is a part of,
     *            its used to calculate the intensity of the light
     */
    public RayNode(final int posX, final int posY, final int nodeLevel,
        final float size) {
        level = nodeLevel;
        x = posX;
        y = posY;

        final float distance = (float) Math.sqrt((x * x) + (y * y));
        intensity = (1.f - (distance / (size + 0.5f)));
        childrenCount = 0;
    }

    /**
     * Create a ray node at a location. The location is relative to the origin
     * of the light.
     * 
     * @param loc the location of the ray node relative to the origin of the
     *            light
     * @param nodeLevel the level of the node, means how many steps the node is
     *            away from the origin of the light
     * @param size the length of the rays of the light this node is a part of,
     *            its used to calculate the intensity of the light
     */
    public RayNode(final Location loc, final int nodeLevel, final float size) {
        this(loc.getScX(), loc.getScY(), nodeLevel, size);
    }

    /**
     * Create a ray to a existing ray. The path is generated by Bresenham with a
     * defined length. In case the node added in this step exists already, the
     * there is no node added in this step and if the node is still not created
     * its created now. This function is called recursively up until the length
     * of the path.
     * 
     * @param xPath the array of relative x coordinates to the parent node
     * @param yPath the array of relative y coordinates to the parent node
     * @param len the length of the ray, means the coordinate values in the
     *            arrays for this ray
     * @param index the current position on the ray
     * @param size real length of the ray
     */
    public void addRay(final int[] xPath, final int[] yPath, final int len,
        final int index, final float size) {
        // there are more points on the path
        if (index < len) {
            final int nx = xPath[index];
            final int ny = yPath[index];

            // search matching point
            RayNode next = null;
            if (children != null) {
                for (final RayNode node : children) {
                    if (node == null) {
                        break;
                    } else if ((node.x == nx) && (node.y == ny)) {
                        next = node;
                        break;
                    }
                }
            }

            // create it if not found
            if (next == null) {
                next = new RayNode(nx, ny, index, size);
                // only inside falloff circle
                if (next.intensity > 0) {
                    children[childrenCount++] = next;
                }
            }

            // recursively add rest of path
            next.addRay(xPath, yPath, len, index + 1, size);
        }
    }

    /**
     * Apply this ray node and all its children to a light map. The light
     * intensity is set for the tile and the intensity is reduced by the light
     * blocking level of the tile. In case the tile blocks the light entirely
     * the ray stops at this location and the following ray nodes are not
     * rendered applies anymore, that leads to the point that the light has no
     * influence anymore.
     * 
     * @param shadowMap the map that is the target of the apply operation and
     *            gives the data how much light is blocked out by the tiles
     * @param globalIntensity global intensity modificator that reduces the
     *            default intensity of the light by the glowing intensity of the
     *            light in order to make the light generally weaker
     */
    public void apply(final LightSource shadowMap, final float globalIntensity) {
        int blocked =
            shadowMap.setIntensity(x, y, globalIntensity * intensity);
        float newIntensity = globalIntensity;
        // never block light source itself, remove when blocking is variable
        if (level == 0) {
            blocked = 0;
        }

        if ((children != null) && (blocked < LightingMap.BLOCKED_VIEW)) {
            if (blocked > 0) {
                newIntensity -= blocked / (float) LightingMap.BLOCKED_VIEW;
            }

            if (newIntensity > 0.05) {
                for (final RayNode node : children) {
                    if (node == null) {
                        break;
                    }
                    node.apply(shadowMap, newIntensity);
                }
            }
        }
    }

    /**
     * Dump the data of all ray nodes from this node and all its children out.
     */
    public void dump() {
        LOGGER.debug(toString());
        if (children != null) {
            for (final RayNode node : children) {
                if (node == null) {
                    break;
                }
                node.dump();
            }
        }
    }

    /**
     * Create a string representation of this ray node.
     * 
     * @return the string representation of this ray node
     * @see java.lang.Object#toString()
     */
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        BUILDER.setLength(0);
        for (int i = 0; i < level; i++) {
            BUILDER.append("--");
        }
        BUILDER.append("(");
        BUILDER.append(x);
        BUILDER.append(",");
        BUILDER.append(y);
        BUILDER.append(")");
        return BUILDER.toString();
    }
}
