/*-
 * Copyright (c) 2021-2025 Red Hat, Inc.
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
package io.kojan.mbici.report;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Function;

/**
 * @author Mikolaj Izdebski
 */
abstract class Report {
    private final StringBuilder sb = new StringBuilder();

    private static final String STATIC =
            "https://mbi-artifacts.s3.eu-central-1.amazonaws.com/static";

    public static String link(String href, String text) {
        return "<a href='" + href + "'>" + text + "</a>";
    }

    public void addNoNL(CharSequence... content) {
        for (CharSequence s : content) {
            sb.append(s);
        }
    }

    public void add(CharSequence... content) {
        addNoNL(content);
        sb.append('\n');
    }

    public void header(String title) {
        add("<!doctype html>");
        add("<html lang='en'>");

        add("<head>");
        add("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/>");
        add("<meta charset='utf-8'>");
        add("<meta name='viewport' content='width=device-width, initial-scale=1'>");
        add("<title>MBI &ndash; ", title, "</title>");
        add(
                "<link href='",
                STATIC,
                "/fontawesome/css/all.min.css' type='text/css' rel='stylesheet'>");
        add(
                "<link href='",
                STATIC,
                "/bootstrap/css/bootstrap.min.css' type='text/css' rel='stylesheet'>");
        add("<link href='", STATIC, "/custom.css' rel='stylesheet'>");
        add("</head>");

        add("<body>");
        add("<nav class='navbar navbar-expand-md navbar-dark bg-primary fixed-top'>");
        add("<div class='container-fluid'>");

        add("<svg class='logo' style='font-size:65px' height='47' width='150'>");
        add(
                "<svg y='0' height='4' width='150' viewbox='0 0 150 4'><text x='-6' y='47'>MBI</text></svg>");
        add(
                "<svg y='6' height='4' width='150' viewbox='0 6 150 4'><text x='-6' y='47'>MBI</text></svg>");
        add(
                "<svg y='12' height='4' width='150' viewbox='0 12 150 4'><text x='-6' y='47'>MBI</text></svg>");
        add(
                "<svg y='18' height='4' width='150' viewbox='0 18 150 4'><text x='-6' y='47'>MBI</text></svg>");
        add(
                "<svg y='24' height='4' width='150' viewbox='0 24 150 4'><text x='-6' y='47'>MBI</text></svg>");
        add(
                "<svg y='30' height='4' width='150' viewbox='0 30 150 4'><text x='-6' y='47'>MBI</text></svg>");
        add(
                "<svg y='36' height='4' width='150' viewbox='0 36 150 4'><text x='-6' y='47'>MBI</text></svg>");
        add(
                "<svg y='42' height='4' width='150' viewbox='0 42 150 4'><text x='-6' y='47'>MBI</text></svg>");
        add("</svg>");

        add("<a class='navbar-brand' href='/'>CI Test Results</a>");
        add("<div class='collapse navbar-collapse'>");
        add("<ul class='navbar-nav me-auto mb-2 mb-md-0'>");
        add("<li class='nav-item'><a class='nav-link' href='result.html'>Outcome</a></li>");
        add("<li class='nav-item'><a class='nav-link' href='platform.html'>Platform</a></li>");
        add("<li class='nav-item'><a class='nav-link' href='subject.html'>Subject</a></li>");
        add("<li class='nav-item'><a class='nav-link' href='plan.html'>Plan</a></li>");
        add("</ul>");
        add("</div>");

        add("</div>");
        add("</nav>");
        add("<main class='container'>");
        add("<h2>", title, "</h2>");
    }

    public void footer() {
        add("</main>");
        add("<script src='", STATIC, "/bootstrap/js/bootstrap.bundle.min.js'></script>");
        add("</body>");
        add("</html>");
    }

    public void wrap(String tag, String... content) {
        addNoNL("<" + tag + ">");
        addNoNL(content);
        add("</" + tag + ">");
    }

    public void para(String... content) {
        wrap("p", content);
    }

    public void item(String... content) {
        wrap("li", content);
    }

    public void subtitle(String s) {
        add("<h4>", s, "</h4>");
    }

    public <T> void list(String s1, String s2, Collection<T> col, Function<T, String> s) {
        if (col.isEmpty()) {
            para(s2);
        } else {
            add(s1);
            add("<ul>");
            col.stream().map(s).forEach(this::item);
            add("</ul>");
        }
    }

    protected abstract void body();

    public void publish(Path path) throws IOException {
        body();

        try (Writer writer = Files.newBufferedWriter(path)) {
            writer.write(sb.toString());
        }
    }
}
