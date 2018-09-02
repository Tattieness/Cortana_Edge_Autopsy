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

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 *
 * @author Clare
 */
public class EsentColData {

    long longValue; // Return variable for Longs
    String stringValue; // Return variable for Strings
    float floatValue; // Return variable for float
    byte byteValue; // Return variable for byte
    short shortValue; // Return variable for short
    int intValue;  // Return variable for int
    byte[] byteValueArray;

    public EsentColData getColumnData(Pointer psesidVal, Pointer ptableidVal, long colId, long colType, long colSize) {

        PointerByReference pcbActual = new PointerByReference(); // Native Long for the size of the returning the column data
        NativeLong jetGrbit = new NativeLong(); // Native Long for jetGrbit - used to set any required flags as bits
        jetGrbit.setValue(0); // No flags required - set to 0;
        NativeLong cbData = new NativeLong(); // Used to define the maximum length of the column
        cbData.setValue(colSize);  // cbdata is set to the maximum defined in the JET_COLUMNBASE structure passed in via pColValId 
        NativeLong icolId = new NativeLong(); // Identifier of the column to fetch the data for
        icolId.setValue(colId); // Seting the Identifier of the column to fetch the data for
        int jetErr = 0; //  jetErr return value 

        if (colSize != 0) {  // if the column metadata has a column size of zero ignore the request
            Memory buff = new Memory(colSize); // Set the memory buffer to the defined size of the column

            jetErr = EsentLibrary.INSTANCE.JetRetrieveColumn(psesidVal, ptableidVal, icolId, buff, cbData, pcbActual, jetGrbit, null);  // esent library call to retreive the column data.
            
            if (colType == 1 || colType == 2) { // Value is a byte
                byteValue = buff.getByte(0);
            }
            if (colType == 3) { // Value is a short
                shortValue = buff.getShort(0);
            }
            if (colType == 6 || colType == 7 || colType == 8) { // Value is a float
                floatValue = buff.getFloat(0);
            }
            if (colType == 4 || colType == 14) {  // Value is an int
                longValue = buff.getInt(0);
            }
            if (colType == 11 || colType == 15 || colType == 16) { // Value is a long
                longValue = buff.getLong(0);
            }
            if ((colType == 10) || (colType == 12)) { // Value is a wide string
                 stringValue = buff.getWideString(0);
                 if (pcbActual.getPointer().getInt(0) == 0)
                 {
                   stringValue = "";  
                 }
                 else if (pcbActual.getPointer().getInt(0) < stringValue.length())
                 {
                    stringValue =  buff.getWideString(0).substring(0,pcbActual.getPointer().getInt(0));
                 }
                 else
                     stringValue =  buff.getWideString(0);
         
            }
            
            
            buff.clear(); // Cleardown the memory buffer
        }
        return this; // Return this object
    }

}
