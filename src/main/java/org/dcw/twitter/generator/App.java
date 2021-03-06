/*
 * Copyright 2017 Derek Weber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dcw.twitter.generator;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.io.IOException;

public class App {

    public static final String APP_TITLE = "Retweet Generator";

    @Parameter(names = {"-t", "--tweets-file"},
        description = "File with current tweets")
    private String tweetsFile = "./tweets.json";

    @Parameter(names = {"-h", "-?", "--help"}, description = "Help")
    private static boolean help = false;

    public static void main(String[] args) throws IOException {
        App theApp = new App();

        // JCommander instance parses args, populates fields of theApp
        JCommander argsParser = JCommander.newBuilder()
            .addObject(theApp)
            .programName("bin/retweet-generator[.bat]")
            .build();
        try {
            argsParser.parse(args);
        } catch (ParameterException e) {
            System.err.println("Unknown argument parameter:\n  " + e.getMessage());
            help = true;
        }

        if (help) {
            StringBuilder sb = new StringBuilder();
            argsParser.usage(sb);
            System.out.println(sb.toString());
            System.exit(-1);
        }

        theApp.run();
    }

    private void run() throws IOException {
        System.out.println("Running " + APP_TITLE);
        // load model
        final TweetCorpusModel model = new TweetCorpusModel(tweetsFile);

        // create and run GUI
        final JFrame frame = new JFrame(APP_TITLE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        final RetweetGeneratorUI ui = new RetweetGeneratorUI(model);
        frame.setContentPane(ui);
        System.out.println("UI built");

        // Display the frame/window
        frame.setSize(600, 400);
        frame.setVisible(true);

        System.out.println(APP_TITLE + " is now running...");
    }
}
