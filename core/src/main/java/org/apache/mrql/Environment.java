/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.mrql;

import java.io.*;
import org.apache.mrql.gen.Tree;


/** the run-time environment for in-memory evaluation (binds variables to MRData) */
final public class Environment implements Serializable {
    public String name;
    public MRData value;
    public Environment next;

    Environment () {}

    Environment ( String n, MRData v, Environment next ) {
        name = n;
        value = v;
        this.next = next;
    }

    public void replace ( String n, MRData v ) {
        for ( Environment e = this; e != null; e = e.next )
            if (e.name.equals(n)) {
                e.value = v;
                return;
            };
        throw new Error("Cannot find the name "+n+" in the environment "+this);
    }

    public String toString () {
        String s = "[";
        for ( Environment e = this; e != null; e = e.next )
            s += " " + e.name + ": " + e.value;
        return s+" ]";
    }

    final static MRData one = new MR_byte(1);

    private void writeObject ( ObjectOutputStream out ) throws IOException {
        if (value == null || value instanceof Lambda) {
            out.writeUTF("");
            one.write(out);
        } else {
            out.writeUTF(name);
            value.write(out);
        };
        if (next == null)
            out.writeByte(0);
        else {
            out.writeByte(1);
            next.writeObject(out);
        }
    }

    private void readObject ( ObjectInputStream in ) throws IOException, ClassNotFoundException {
        name = in.readUTF();
        name = Tree.add(name);
        value = MRContainer.read(in);
        if ( in.readByte() > 0 ) {
            next = new Environment();
            next.readObject(in);
        } else next = null;
    }
}
