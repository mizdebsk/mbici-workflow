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
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

import javax.xml.stream.XMLStreamException;

import org.fedoraproject.mbi.ci.generate.GenerateCommand;
import org.fedoraproject.mbi.ci.report.ReportCommand;
import org.fedoraproject.mbi.ci.run.RunCommand;
import org.fedoraproject.mbi.xml.Builder;
import org.fedoraproject.mbi.xml.Constituent;
import org.fedoraproject.mbi.xml.Entity;
import org.fedoraproject.mbi.xml.XMLDumper;

/**
 * @author Mikolaj Izdebski
 */
class ArgParser
{
    /**
     * Parse command line by first converting it to XML and then parsing XML to a POJO.
     * <p>
     * For example commant <tt>java -ea -cp foo:bar main.Clazz arg1 arg2</tt> is converted to the following XML:
     *
     * <pre>
     * &lt;java&gt;
     *   &lt;ea&gt;true&lt;/ea&gt;
     *   &lt;cp&gt;foo:bar&lt;/cp&gt;
     *   &lt;argument&gt;main.Clazz&lt;/argument&gt;
     *   &lt;argument&gt;arg1&lt;/argument&gt;
     *   &lt;argument&gt;arg2&lt;/argument&gt;
     * &lt;/java&gt;
     * </pre>
     */
    static <T> T parse( Entity<T, ? extends Builder<T>> entity, String commandName, Queue<String> args )
    {
        try
        {
            StringWriter sw = new StringWriter();
            XMLDumper xd = new XMLDumper( sw );
            xd.dumpStartDocument();
            xd.dumpStartElement( commandName );
            while ( !args.isEmpty() )
            {
                xd.dumpStartElement( args.peek().startsWith( "-" ) ? args.remove().substring( 1 ) : "argument" );
                xd.dumpText( args.isEmpty() || args.peek().startsWith( "-" ) ? "true" : args.remove() );
                xd.dumpEndElement();
            }
            xd.dumpEndElement();
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

    /**
     * Print usage help for given command entity.
     */
    static void help( Entity<?, ?> entity )
    {
        System.err.println( "Accepted arguments for command \"" + entity.getTag() + "\" are:" );

        int maxArgNameLength = entity.getElements().stream().map( Constituent::getTag ) //
                                     .map( String::length ).max( Comparator.naturalOrder() ).get();
        String spacesString = new String( new char[maxArgNameLength + 5] ).replace( '\0', ' ' );

        for ( var argument : entity.getElements() )
        {
            System.err.print( "  -" + argument.getTag() );
            System.err.print( spacesString.substring( argument.getTag().length() ) );
            System.err.print( "(" );
            System.err.print( argument.isUnique() ? "singular" : "plural" );
            System.err.print( ", " );
            System.err.print( argument.isOptional() ? "optional" : "mandatory" );
            System.err.println( ")" );
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
        var commandEntities = Arrays.asList( GenerateCommand.ENTITY, RunCommand.ENTITY, ReportCommand.ENTITY );

        LinkedList<String> argList = new LinkedList<>( Arrays.asList( args ) );

        if ( argList.isEmpty() )
        {
            System.err.println( "No command specified" );
        }
        else
        {
            String commandName = argList.remove();

            for ( var entity : commandEntities )
            {
                if ( commandName.equals( entity.getTag() ) )
                {
                    if ( !argList.isEmpty() && argList.peek().equals( "-help" ) )
                    {
                        ArgParser.help( entity );
                        return;
                    }
                    ArgParser.parse( entity, commandName, argList ).run();
                    return;
                }
            }

            System.err.println( "Unknown command: " + commandName );
        }

        System.err.println( "Available commands are:" );
        for ( var entity : commandEntities )
        {
            System.err.println( "  " + entity.getTag() );
        }
        System.exit( 1 );

    }
}
