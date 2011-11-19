/*
 * This file is part of the Illarion Client.
 *
 * Copyright © 2011 - Illarion e.V.
 *
 * The Illarion Client is free software: you can redistribute i and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * The Illarion Client is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * the Illarion Client. If not, see <http://www.gnu.org/licenses/>.
 */
package illarion.client.util;

/**
 * This interface is used for a class that is able to receive the path
 * calculated by the pathfinder.
 * 
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public interface PathReceiver {
    /**
     * Handle the generated path.
     * 
     * @param path use the path that was generated
     */
    void handlePath(Path path);
}
