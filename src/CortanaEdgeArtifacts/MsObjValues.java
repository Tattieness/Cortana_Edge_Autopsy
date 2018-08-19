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
//import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 *
 * @author Clare
 */
public class MsObjValues {

    long objidTableValue;   // ID Number for each Table
    long typeValue;         // Type Value for entry 1 = Table, 2 = Column ignore other entries in this case 
    String objNameValue;    // Name of the Table or Columns

    EsentLibrary.JET_COLUMNBASE pColValId = new EsentLibrary.JET_COLUMNBASE();  // Instantiates a structure sefination in memory for JET_COLUMNBASE 

    public MsObjValues getColumns(Pointer psesidVal, Pointer ptableidVal) {

        String objidTable = "ObjidTable";   // Name of the Column whose value is being retrieved
        String type = "Type";  // Name of the Column whose value is being retrieved
        String name = "Name";  // Name of the Column whose value is being retrieved
        String stringColData; // Return variable for Strings
        long longColData;  // Return variable for Longs

    
        pColValId = getColumnInfo(psesidVal, ptableidVal, objidTable); // Get column metadata
        longColData = getLongColumnValue(psesidVal, ptableidVal, pColValId); // Get column value
        objidTableValue = longColData; // Returns the long value for the column

     
        pColValId = getColumnInfo(psesidVal, ptableidVal, type);  // Get column metadata
        longColData = getLongColumnValue(psesidVal, ptableidVal, pColValId); // Get column value
        typeValue = longColData; // Returns the long value for the column


        pColValId = getColumnInfo(psesidVal, ptableidVal, name); // Get column metadata
        stringColData = getStringColumnValue(psesidVal, ptableidVal, pColValId); // Get column value
        objNameValue = stringColData; // Returns the string value for the column

        return this; // returns this JET_COLUMNBASE structure
    }

    public EsentLibrary.JET_COLUMNBASE getColumnInfo(Pointer psesidVal, Pointer ptableidVal, String szColName) {
        NativeLong colcbMax = new NativeLong(); // Native Long for the maximum buffer size to return the column metadata
        colcbMax.setValue(32768);  // Defines the maximum buffer size to return the column metadata
        NativeLong colInfoLevel = new NativeLong(); // Native Long to defines the type of information to be returned for the column;
        colInfoLevel.setValue(EsentLibrary.JET_ColInfoBase);  // Defines the type of information to be returned for the column to be JET_COLUMNBASE;
        EsentLibrary.JET_COLUMNBASE pColVal = new EsentLibrary.JET_COLUMNBASE();  // Instantiates a JET_COLUMNBASE structure
        int jetErr = 0; // jetErr return value 

        jetErr = EsentLibrary.INSTANCE.JetGetTableColumnInfo(psesidVal, ptableidVal, szColName, pColVal, colcbMax, colInfoLevel); // esent library call to retreive the column meta data.
        return pColVal; // returns the JET_COLUMNBASE structure
    }

    public String getStringColumnValue(Pointer psesidVal, Pointer ptableidVal, EsentLibrary.JET_COLUMNBASE pColValId) {

        PointerByReference pcbActual = new PointerByReference();  // Pointer by Reference for pcbActual will hold the value in bytes of the data retrieved
        NativeLong jetGrbit = new NativeLong(); // Native Long for jetGrbit - used to set any required flags as bits
        jetGrbit.setValue(0); // No flags required - set to 0;
        NativeLong cbData = new NativeLong(); // Used to define the maximum length of the column
        cbData.setValue(pColValId.cbMax.intValue()); // cbdata is set to the maximum defined in the JET_COLUMNBASE structure passed in via pColValId 
        String s = "";  // return variable for the String values
        int jetErr = 0; // jetErr return value 

        Memory buff = new Memory(pColValId.cbMax.intValue());  // Set the memory buffer to the defined size of the column
        jetErr = EsentLibrary.INSTANCE.JetRetrieveColumn(psesidVal, ptableidVal, pColValId.columnid, buff, cbData, pcbActual, jetGrbit, null); // esent library call to retreive the column data.
        s = buff.getString(0).substring(0, pcbActual.getPointer().getInt(0)); // Retrived the string from memory using the actual column data size

        buff.clear();  // Cleardown the memory buffer

        return s;  //Return the string value
    }

    public Long getLongColumnValue(Pointer psesidVal, Pointer ptableidVal, EsentLibrary.JET_COLUMNBASE pColValId) {

        PointerByReference pcbActual = new PointerByReference(); // Pointer by Reference for pcbActual will hold the value in bytes of the data retrieved
        NativeLong jetGrbit = new NativeLong(); // Native Long for jetGrbit - used to set any required flags as bits
        jetGrbit.setValue(0); // No flags required - set to 0;
        NativeLong cbData = new NativeLong();   // Used to define the maximum length of the column
        cbData.setValue(pColValId.cbMax.intValue()); // cbdata is set to the maximum defined in the JET_COLUMNBASE structure passed in via pColValId 
        int jetErr = 0; // jetErr return value 
        long l = 0; // Return variable for the Long values
        int colType; // Used to indentify the data type

        Memory buff = new Memory(pColValId.cbMax.intValue()); // Set the memory buffer to the defined size of the column
        jetErr = EsentLibrary.INSTANCE.JetRetrieveColumn(psesidVal, ptableidVal, pColValId.columnid, buff, cbData, pcbActual, jetGrbit, null);  // esent library call to retreive the column data.
        
        
        colType = pColValId.coltyp.intValue(); // Retrived the column type defined in the JET_COLUMNBASE structure passed in via pColValId
        
        if (colType == 1 || colType == 2) {   // Value is a byte so retrieve a single byte and cast to a long
            l = buff.getByte(0);
        }
        if (colType == 4 || colType == 14) { // Value is an int and cast to a long
            l = buff.getInt(0);
        }
        if (colType == 11 || colType == 15 || colType == 16) { // Value is a long 
            l = buff.getLong(0);
        }
        if (colType == 3) {  // Value is a short and cast to a long
            l = buff.getShort(0);
        }
        buff.clear(); // cleardown the memory buffer

        return l; //return the long value
    }

}
