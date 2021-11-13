/*-
 * Copyright (c) 2021 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fedoraproject.mbi.ci;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import javax.xml.stream.XMLStreamException;

import org.fedoraproject.mbi.ci.generate.GenerateCommand;
import org.fedoraproject.mbi.ci.report.ReportCommand;
import org.fedoraproject.mbi.ci.run.RunCommand;
import org.fedoraproject.mbi.xml.Builder;
import org.fedoraproject.mbi.xml.Entity;
import org.fedoraproject.mbi.xml.XMLDumper;

/**
 * @author Mikolaj Izdebski
 */
class ArgParser
{
    static <T> T parse( Entity<T, ? extends Builder<T>> entity, Queue<String> args )
    {
        try
        {
            StringWriter sw = new StringWriter();
            XMLDumper xd = new XMLDumper( sw );
            xd.dumpStartDocument();
            if ( !args.isEmpty() )
            {
                xd.dumpStartElement( args.remove() );
                while ( !args.isEmpty() )
                {
                    xd.dumpStartElement( args.peek().startsWith( "-" ) ? args.remove().substring( 1 ) : "argument" );
                    xd.dumpText( args.isEmpty() || args.peek().startsWith( "-" ) ? "true" : args.remove() );
                    xd.dumpEndElement();
                }
                xd.dumpEndElement();
            }
            xd.dumpEndDocument();
            return entity.readFromXML( new StringReader( sw.toString() ) );
        }
        catch ( XMLStreamException e )
        {
            System.err.println( e.getMessage() );
            System.exit( 1 );
            return null;
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }
}

/**
 * @author Mikolaj Izdebski
 */
public class Main
{
    public static void main( String[] args )
        throws Exception
    {
        LinkedList<String> argList = new LinkedList<>( Arrays.asList( args ) );
        if ( argList.isEmpty() )
        {
            argList.add( "help" );
        }

        var commandEntities = Arrays.asList( GenerateCommand.ENTITY, RunCommand.ENTITY, ReportCommand.ENTITY );
        for ( var entity : commandEntities )
        {
            if ( argList.peek().equals( entity.getTag() ) )
            {
                Command command = ArgParser.parse( entity, argList );
                command.run();
                return;
            }
        }

        throw new IllegalArgumentException( "Unknown command: " + argList.peek() );
    }
}
