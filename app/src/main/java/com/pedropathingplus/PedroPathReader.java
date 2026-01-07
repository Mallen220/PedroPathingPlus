package com.pedropathingplus;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pedropathing.geometry.Pose;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PedroPathReader {

  public final PedroPP file;
  private final Map<String, Pose> poses = new HashMap<>();

  private double lastX;
  private double lastY;
  private double lastDeg;

  public PedroPathReader(String filename, Context context) throws IOException {
    InputStream stream = null;
    try {
      stream = context.getAssets().open("AutoPaths/" + filename + ".pp");
    } catch (Exception e) {
       // try without extension
        try {
            stream = context.getAssets().open("AutoPaths/" + filename);
        } catch (IOException e2) {
            throw e2;
        }
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

      if (line.controlPoints != null) {
          for (int i = 0; i < line.controlPoints.size(); i++) {
              PedroPP.Point cp = line.controlPoints.get(i);
              poses.put(name + "_control" + (i+1), toPose(cp.x, cp.y, 0));
          }
      }

      lastX = lx;
      lastY = ly;
      lastDeg = heading;
    }
  }

  public Pose get(String name) {
    return poses.get(name);
  }

  /**
   * Converts raw PP coordinates (usually Landscape) to PedroPathing Poses.
   * Logic: Y -> X, 144 - X -> Y, deg - 90 -> radians.
   */
  public static Pose toPose(double x, double y, double deg) {
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
  public List<SequenceItem> sequence;

  public static class StartPoint {
    public double x;
    public double y;
    public String heading;
    public double startDeg;
    public double endDeg;
  }

  public static class Line {
    public String id;
    public String name;
    public EndPoint endPoint;
    public List<Point> controlPoints;
    public List<EventMarker> eventMarkers;
    public String color;
    public int waitBeforeMs;
    public int waitAfterMs;
  }

  public static class EndPoint {
    public double x;
    public double y;
    public String heading;
    public boolean reverse;
  }

  public static class Point {
      public double x;
      public double y;
  }

  public static class EventMarker {
      public String id;
      public String name;
      public double position; // 0.0 to 1.0
      public int lineIndex; // Sometimes used
      public String commandId;
  }

  public static class SequenceItem {
      public String kind; // "path"
      public String lineId;
  }
}
