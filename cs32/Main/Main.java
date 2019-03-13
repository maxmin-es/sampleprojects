package edu.brown.cs.mmines.Main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

import edu.brown.cs.mmines.ApplicationInputs.CommandManager;
import edu.brown.cs.mmines.ApplicationInputs.Repl;
import edu.brown.cs.mmines.AutoCorrect.AutoCorrectUniverse;
import edu.brown.cs.mmines.Bacon.BaconUniverse;
import edu.brown.cs.mmines.stars.Star;
import edu.brown.cs.mmines.stars.StarsUniverse;
import freemarker.template.Configuration;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import spark.ExceptionHandler;
import spark.ModelAndView;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;
import spark.TemplateViewRoute;
import spark.template.freemarker.FreeMarkerEngine;

/**
 * The Main class of our project. This is where execution begins.
 *
 * @author mmines
 */
public final class Main {
  private static CommandManager cm = new CommandManager();
  private static StarsUniverse starsUni = new StarsUniverse();
  private static BaconUniverse baconUni = new BaconUniverse();
  private static AutoCorrectUniverse acUni = new AutoCorrectUniverse();
  private static final Gson GSON = new Gson();

  private static final int DEFAULT_PORT = 4567;

  /**
   * The initial method called when execution begins.
   *
   * @param args
   *          An array of command line arguments
   */
  public static void main(String[] args) {
    new Main(args).run();
  }

  private String[] args;

  private Main(String[] args) {
    this.args = args;
  }

  private void run() {
    // Parse command line arguments
    OptionParser parser = new OptionParser();
    parser.accepts("gui");
    parser.accepts("port").withRequiredArg().ofType(Integer.class)
        .defaultsTo(DEFAULT_PORT);
    OptionSet options = parser.parse(args);

    if (options.has("gui")) {
      runSparkServer((int) options.valueOf("port"));
    }
    starsUni.installCommands(cm);
    acUni.installCommands(cm);
    baconUni.installCommands(cm);

    Repl repl = new Repl(cm);
    repl.run();
  }

  private static FreeMarkerEngine createEngine() {
    Configuration config = new Configuration();
    File templates = new File("src/main/resources/spark/template/freemarker");
    try {
      config.setDirectoryForTemplateLoading(templates);
    } catch (IOException ioe) {
      System.out.printf("ERROR: Unable use %s for template loading.%n",
          templates);
      System.exit(1);
    }
    return new FreeMarkerEngine(config);
  }

  private void runSparkServer(int port) {
    Spark.port(port);
    Spark.externalStaticFileLocation("src/main/resources/static");
    Spark.exception(Exception.class, new ExceptionPrinter());

    FreeMarkerEngine freeMarker = createEngine();

    // Setup Spark Routes
    Spark.get("/stars", new FrontHandler(), freeMarker);
    Spark.post("/results", new SubmitHandler(), freeMarker);
    Spark.get("/autocorrect", new ACFrontHandler(), freeMarker);
    Spark.post("/autocorrect", new ACResponseHandler());
    Spark.get("/bacon", new BaconFrontHandler(), freeMarker);
    Spark.post("/bacon", new BaconAutocorrectHandler());
    Spark.post("/baconpath", new BaconPathHandler(), freeMarker);
    Spark.get("bacon/actor/:id", new ActorHandler(), freeMarker);
    Spark.get("bacon/film/:id", new FilmHandler(), freeMarker);

  }

  /**
   * Handle requests to the front page of our Stars website.
   *
   * @author mmines
   */
  private static class FrontHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      Map<String, Object> variables = ImmutableMap.of("title",
          "Stars: Query the database", "result", "");
      return new ModelAndView(variables, "query.ftl");
    }
  }

  /**
   * Method for handling actor links.
   *
   * @author maxmines
   *
   */
  private static class ActorHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) throws SQLException {
      String realID = URLDecoder.decode(req.params(":id"));
      String actorName = baconUni.getActorName(realID);
      List<String> films = baconUni.getFilmsOfActor(realID);

      Map<String, Object> variables = ImmutableMap.of("title", actorName,
          "result", "", "films", films);
      return new ModelAndView(variables, "actor.ftl");
    }
  }

  /**
   * Method for handling film links.
   *
   * @author maxmines
   *
   */
  private static class FilmHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) throws SQLException {
      String realID = URLDecoder.decode(req.params(":id"));
      String filmName = baconUni.getFilmName(realID);
      List<String> actors = baconUni.getActorsOfFilm(realID);

      Map<String, Object> variables = ImmutableMap.of("title", filmName,
          "result", "", "films", actors);
      return new ModelAndView(variables, "actor.ftl");
    }
  }

  /**
   * Handle requests to the front page of our Bacon website.
   *
   * @author maxmines
   *
   */
  private static class BaconFrontHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      Map<String, Object> variables = ImmutableMap.of("title", "Bacon",
          "result", "", "links", new LinkedList<String>(), "results", "");
      return new ModelAndView(variables, "bacon_query.ftl");
    }
  }

  /**
   * Handler for updating suggestions in search boxes.
   *
   * @author maxmines
   *
   */
  private static class BaconAutocorrectHandler implements Route {
    @Override
    public String handle(Request req, Response res) {
      QueryParamsMap qm = req.queryMap();
      String txtResp = qm.value("fromBox");
      List<String> suggestions = baconUni.getAC().fromBoxToResults(txtResp);
      List<String> suggsUpper = new LinkedList<String>();
      for (String s : suggestions) {
        String[] chopped = s.split(" ");
        String rejoined = "";
        for (String sub : chopped) {
          if (!sub.equals("")) {
            String uppered = sub.substring(0, 1).toUpperCase()
                + sub.substring(1);
            rejoined = rejoined + uppered + " ";
          }
        }
        rejoined = rejoined.substring(0, rejoined.length() - 1);
        suggsUpper.add(rejoined);

      }
      Map<String, Object> variables = ImmutableMap.of("title", "Autocorrect",
          "links", new LinkedList<String>(), "suggestions", suggsUpper);
      return GSON.toJson(variables);
    }
  }

  /**
   * Handles path page.
   *
   * @author maxmines
   *
   */
  private static class BaconPathHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      QueryParamsMap qm = req.queryMap();
      Map<String, Object> variables = null;

      String startActor = qm.value("box1");
      String endActor = qm.value("box2");
      try {
        List<String> path = baconUni.connectNonCommand(startActor, endActor);

        variables = ImmutableMap.of("title", "Autocorrect", "links", path,
            "suggestions", "");
      } catch (IOException e) {
        List<String> l = new LinkedList<String>();
        l.add("Must add database.");
        variables = ImmutableMap.of("title", "Autocorrect", "suggestions", "",
            "links", l);

      } catch (IllegalArgumentException e) {
        List<String> l = new LinkedList<String>();
        l.add("Actor not found");
        variables = ImmutableMap.of("title", "Autocorrect", "links", l,
            "suggestions", "");
      } catch (SQLException e) {
        List<String> l = new LinkedList<String>();
        l.add("Film error.");
        variables = ImmutableMap.of("title", "Autocorrect", "links", l,
            "suggestions", "");
      }

      return new ModelAndView(variables, "bacon_query.ftl");
    }
  }

  /**
   * Handle updating autocorrect page with suggestions.
   *
   * @author mmines
   */
  private static class ACResponseHandler implements Route {
    @Override
    public String handle(Request req, Response res) {
      QueryParamsMap qm = req.queryMap();
      String txtResp = qm.value("fromBox");
      System.out.println(txtResp);
      List<String> suggestions = acUni.fromBoxToResults(txtResp);

      Map<String, Object> variables = ImmutableMap.of("title", "Autocorrect",
          "suggestions", suggestions);
      return GSON.toJson(variables);
    }
  }

  /**
   * Handle requests to the front page of our autocorrect website.
   *
   * @author mmines
   */
  private static class ACFrontHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      Map<String, Object> variables = ImmutableMap.of("title",
          "Stars: Query the database");
      return new ModelAndView(variables, "autocorrect_query.ftl");
    }
  }

  /**
   * Handle requests after user submits information.
   *
   * @author maxmines
   *
   */
  private static class SubmitHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      QueryParamsMap qm = req.queryMap();

      String txtResp = qm.value("text");
      System.out.println(txtResp);
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      cm.process(txtResp, pw);
      String resultFromHandler = sw.toString();
      Map<String, Object> variables = null;
      System.out.println(resultFromHandler);

      if (resultFromHandler.startsWith("ERROR")
          || resultFromHandler.startsWith("Read")) {
        variables = ImmutableMap.of("title", "Stars: Query the database",
            "results", resultFromHandler, "stars", new LinkedList<Star>());
      } else {
        variables = ImmutableMap.of("title", "Stars: Query the database",
            "results", "", "stars", starsUni.returnResults());
      }
      return new ModelAndView(variables, "results.ftl");
    }
  }

  /**
   * Display an error page when an exception occurs in the server.
   *
   * @author jj
   */
  private static class ExceptionPrinter implements ExceptionHandler {
    @Override
    public void handle(Exception e, Request req, Response res) {
      res.status(500);
      StringWriter stacktrace = new StringWriter();
      try (PrintWriter pw = new PrintWriter(stacktrace)) {
        pw.println("<pre>");
        e.printStackTrace(pw);
        pw.println("</pre>");
      }
      res.body(stacktrace.toString());
    }
  }
}
