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
package org.fedoraproject.mbi.ci.report;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Mikolaj Izdebski
 */
class Report
{
    private final StringBuilder sb = new StringBuilder();

    public void add( CharSequence... content )
    {
        for ( CharSequence s : content )
        {
            sb.append( s );
        }
        sb.append( '\n' );
    }

    public void addHeader()
    {
        add( "<!doctype html>" );
        add( "<html lang='en'>" );

        add( "<head>" );
        add( "<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/>" );
        add( "<meta charset='utf-8'>" );
        add( "<meta name='viewport' content='width=device-width, initial-scale=1'>" );
        add( "<title>MBI &ndash; Test results</title>" );
        add( "<link href='/static/fontawesome/css/all.min.css' type='text/css' rel='stylesheet'>" );
        add( "<link href='/static/bootstrap/css/bootstrap.min.css' type='text/css' rel='stylesheet'>" );
        add( "<link href='/static/custom.css' rel='stylesheet'>" );
        add( "</head>" );

        add( "<body>" );
        add( "<nav class='navbar navbar-expand-md navbar-dark bg-primary fixed-top'>" );
        add( "<div class='container-fluid'>" );

        add( "<svg class='logo' style='font-size:65px' height='47' width='150'>" );
        add( "<svg y='0' height='4' width='150' viewbox='0 0 150 4'><text x='-6' y='47'>MBI</text></svg>" );
        add( "<svg y='6' height='4' width='150' viewbox='0 6 150 4'><text x='-6' y='47'>MBI</text></svg>" );
        add( "<svg y='12' height='4' width='150' viewbox='0 12 150 4'><text x='-6' y='47'>MBI</text></svg>" );
        add( "<svg y='18' height='4' width='150' viewbox='0 18 150 4'><text x='-6' y='47'>MBI</text></svg>" );
        add( "<svg y='24' height='4' width='150' viewbox='0 24 150 4'><text x='-6' y='47'>MBI</text></svg>" );
        add( "<svg y='30' height='4' width='150' viewbox='0 30 150 4'><text x='-6' y='47'>MBI</text></svg>" );
        add( "<svg y='36' height='4' width='150' viewbox='0 36 150 4'><text x='-6' y='47'>MBI</text></svg>" );
        add( "<svg y='42' height='4' width='150' viewbox='0 42 150 4'><text x='-6' y='47'>MBI</text></svg>" );
        add( "</svg>" );

        add( "<a class='navbar-brand' href='/'>CI Test Results</a>" );
        add( "<div class='collapse navbar-collapse'></div>" );
        add( "</div>" );
        add( "</nav>" );
        add( "<main class='container'>" );
        add( "<h2>Test results</h2>" );
    }

    public void addFooter()
    {
        add( "</main>" );
        add( "<script src='/static/bootstrap/js/bootstrap.bundle.min.js'></script>" );
        add( "</body>" );
        add( "</html>" );
    }

    public void write( Path path )
        throws IOException
    {
        try ( Writer writer = Files.newBufferedWriter( path ) )
        {
            writer.write( sb.toString() );
        }
    }
}
