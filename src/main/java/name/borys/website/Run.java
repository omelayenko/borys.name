/*
Copyright 2015 Borys Omelayenko

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package name.borys.website;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

import freemarker.template.Configuration;

/**
 * Copies your source content structure to the destination,
 * replacing FTL FreeMarker template files with HTML.
 *
 * @author Borys Omelayenko
 *
 */
public class Run {

    public static void main(String[] args) throws Exception {

        Options options = new Options();
        options.addOption("c", "content", true, "Source content location");
        options.addOption("d", "destination", true, "Destination to write web site files to");
        // options.addOption("i", "images", false, "Include image processing");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        File srcDir = new File(cmd.getOptionValue("c"));

        File dstDir = new File(cmd.getOptionValue("d"));
        dstDir.mkdirs();
        FileUtils.cleanDirectory(dstDir);

        FileUtils.copyDirectory(srcDir, dstDir);

        Properties properties = loadIfExists(null, new File(dstDir, "/templates/templates.properties"));
        for (File topicDir : dstDir.listFiles((FileFilter)DirectoryFileFilter.DIRECTORY)) {
            if (!topicDir.getCanonicalPath().endsWith("templates")) {
                String topic = topicDir.getName();
                properties.setProperty(
                        "topic_" + topic,
                        makeTopic(topicDir, topic));
            }
        }

        for (File ftl : FileUtils.listFiles(dstDir, new String[] {"ftl"}, false)) {
            String story = StringUtils.removeEnd(ftl.getName(), ".ftl");

            Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
            cfg.setDirectoryForTemplateLoading(dstDir);
            cfg.setDefaultEncoding(CharEncoding.UTF_8);

            String htmlFileName = dstDir.getAbsolutePath() + "/" + story + ".html";
            Properties thisFileProps = loadIfExists(properties, new File(story + ".properties"));
            properties.setProperty("pathToStory", story);

            Writer out = new FileWriter(htmlFileName);
            cfg.getTemplate(story + ".ftl", CharEncoding.UTF_8).process(thisFileProps, out);
            out.close();

            new File(dstDir, story + ".ftl").delete();
            new File(dstDir, story + ".properties").delete();
        }
        FileUtils.deleteDirectory(new File(dstDir, "templates"));
    }

    static String makeTopic(File topicDir, String topic) throws Exception {

        String index = "";
        for (File ftl : FileUtils.listFiles(topicDir, new String[] {"ftl"}, false)) {
            index += makeStory(ftl.getParentFile(), StringUtils.removeEnd(ftl.getName(), ".ftl"), topic);
        }
        FileUtils.deleteDirectory(new File(topicDir, "templates"));
        return index;
    }

    static String makeStory(File topicDir, String story, String topic) throws Exception {

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
        cfg.setDirectoryForTemplateLoading(topicDir);
        cfg.setDefaultEncoding(CharEncoding.UTF_8);

        Properties properties = loadIfExists(
                loadIfExists(null, new File(topicDir, "/templates/templates.properties")),
                new File(topicDir, story + ".properties"));

        String htmlFileName = topicDir.getAbsolutePath() + "/" + story + ".html";
        StringBuilder index = new StringBuilder();

        properties.setProperty("pathToStory", topic + "/" + story);

        Writer out = new FileWriter(htmlFileName);
        cfg.getTemplate(story + ".ftl", CharEncoding.UTF_8).process(properties, out);
        out.close();

        new File(topicDir, story + ".ftl").delete();
        new File(topicDir, story + ".properties").delete();

        StringWriter stringWriter = new StringWriter();
        cfg.getTemplate("templates/index.ftl", CharEncoding.UTF_8).process(properties, stringWriter);
        index.append(stringWriter.toString());
        return index.toString();
    }

    static Properties loadIfExists(Properties defaults, File file) throws IOException {
        Properties properties = new Properties(defaults);
        if (file.exists()) {
            properties.load(new AutoCloseInputStream(new FileInputStream(file)));
        }
        return properties;
    }

}
