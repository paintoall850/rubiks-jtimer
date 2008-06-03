/*
 * Rubik's JTimer - Copyright (C) 2008 Doug Li
 * JNetCube - Copyright (C) 2007 Chris Hunt
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 *
 */

import java.io.File;

public class TextFileFilter extends javax.swing.filechooser.FileFilter{
    public String getDescription(){
        return "Text Documents (*.txt)";
    }

    public boolean accept(File f){
        if(f.isDirectory())
            return true;
        return f.getName().toLowerCase().endsWith(".txt");
        //String extension = getExtension(f);
        //return extension.equals("txt");
    }
/*
    private String getExtension(File f){
        String ext = "none";
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if((i > 0) && (i < s.length()-1))
            ext = s.substring(i+1).toLowerCase();
        return ext;
    } // end getExtension
*/
}
