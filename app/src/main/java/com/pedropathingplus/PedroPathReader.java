package com.pedropathingplus;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pedropathing.geometry.Pose;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PedroPathReader {

  private final PedroPP file;
  private final Map<String, Pose> poses = new HashMap<>();

  private double lastX;
  private double lastY;
  private double lastDeg;

  public PedroPathReader(String filename, Context context) throws IOException {
    InputStream stream = null;
    try {
      stream = context.getAssets().open("AutoPaths/" + filename);
    } catch (IOException e) {
      throw e;
    }

    if (stream == null) {
      throw new FileNotFoundException("PP File not found: " + filename);
    }

    Gson gson = new GsonBuilder().create();
    try (InputStreamReader reader = new InputStreamReader(stream)) {
      this.file = gson.fromJson(reader, PedroPP.class);
    }

    loadAllPoints();
  }

  private void loadAllPoints() {
    double x = file.startPoint.x;
    double y = file.startPoint.y;
    double deg = file.startPoint.startDeg;
    if (Double.isNaN(deg)) deg = 0;

    lastX = x;
    lastY = y;
    lastDeg = deg;

    poses.put("startPoint", toPose(lastX, lastY, lastDeg));

    for (PedroPP.Line line : file.lines) {
      double lx = line.endPoint.x;
      double ly = line.endPoint.y;

      double heading = extractHeading(line.endPoint.heading, lastX, lastY, lx, ly, lastDeg);

      String name = line.name.replace(" ", "");
      poses.put(name, toPose(lx, ly, heading));

      lastX = lx;
      lastY = ly;
      lastDeg = heading;
    }
  }

  public Pose get(String name) {
    return poses.get(name);
  }

  /**
   * Retrieves a list of available path filenames (ending in .json) from the "AutoPaths" asset directory.
   *
   * @param context The application context.
   * @return A list of filenames, or an empty list if an error occurs or no files are found.
   */
  public static List<String> getAvailablePathNames(Context context) {
    List<String> pathNames = new ArrayList<>();
    try {
      String[] files = context.getAssets().list("AutoPaths");
      if (files != null) {
        for (String file : files) {
          if (file.endsWith(".json")) {
            pathNames.add(file);
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      // Return empty list on failure
      return Collections.emptyList();
    }
    return pathNames;
  }

  private static Pose toPose(double x, double y, double deg) {
    return new Pose(y, 144 - x, Math.toRadians(deg - 90));
  }

  private static double extractHeading(
      String mode, double lastX, double lastY, double x, double y, double lastDeg) {
    double dx = x - lastX;
    double dy = y - lastY;

    if (Math.abs(dx) < 1e-6 && Math.abs(dy) < 1e-6) {
      return lastDeg;
    }

    double linearDeg = Math.toDegrees(Math.atan2(dy, dx));

    if (mode.equals("linear")) return linearDeg;
    if (mode.equals("tangential")) return linearDeg;

    return lastDeg;
  }
}

////////////////////////////////////////////////////////////////////////////////////////////////////
///                                                                                              ///
///  PEDRO PP FILE DEFINITIONS                                                                   ///
///                                                                                              ///
////////////////////////////////////////////////////////////////////////////////////////////////////
class PedroPP {

  public StartPoint startPoint;
  public List<Line> lines;

  public static class StartPoint {
    public double x;
    public double y;
    public String heading;
    public double startDeg;
    public double endDeg;
  }

  public static class Line {
    public String name;
    public EndPoint endPoint;
  }

  public static class EndPoint {
    public double x;
    public double y;
    public String heading;
    public boolean reverse;
  }
}
