/* 
 * Copyright (C) 2018 Clare Taylor
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package CortanaEdgeArtifacts;


import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;


/**
 *
 * @author Clare
 */
public class EsentColInfo {

    EsentLibrary.JET_COLUMNBASE pColValInfo = new EsentLibrary.JET_COLUMNBASE();  // Instantiates a structure sefination in memory for JET_COLUMNBASE 

    
    
    public EsentColInfo getColumns(Pointer psesidVal, Pointer ptableidVal, String colName) {

         pColValInfo = getColumnInfo(psesidVal, ptableidVal, colName);  // Get column metadata
         return this; // returns this JET_COLUMNBASE structure
    }

    EsentLibrary.JET_COLUMNBASE getColumnInfo(Pointer psesidVal, Pointer ptableidVal, String szColName)
    {
        NativeLong colcbMax = new NativeLong(); // Native Long for the maximum buffer size to return the column metadata
        colcbMax.setValue(32768); // Defines the maximum buffer size to return the column metadata
        NativeLong colInfoLevel = new NativeLong(); // Native Long to defines the type of information to be returned for the column;
        colInfoLevel.setValue(EsentLibrary.JET_ColInfoBase); // Defines the type of information to be returned for the column to be JET_COLUMNBASE;
        EsentLibrary.JET_COLUMNBASE pColInf = new EsentLibrary.JET_COLUMNBASE(); // Instantiates a JET_COLUMNBASE structure
        int jetErr = 0; // jetErr return value 

        jetErr = EsentLibrary.INSTANCE.JetGetTableColumnInfo(psesidVal, ptableidVal, szColName, pColInf, colcbMax, colInfoLevel); // esent library call to retreive the column meta data.

        return pColInf;  // returns the JET_COLUMNBASE structure
    }
}