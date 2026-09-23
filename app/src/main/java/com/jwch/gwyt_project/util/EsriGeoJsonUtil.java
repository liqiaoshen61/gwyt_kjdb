package com.jwch.gwyt_project.util;



import com.alibaba.fastjson.JSON;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EsriGeoJsonUtil {
    //EsriJson转geoJson
    public static String esri2geo(String ersiJson){
        Map geoMap = new HashMap();
        try {
            List geoFs = new ArrayList();
            geoMap.put("type", "FeatureCollection");
            Map esriMap = (Map) JSON.parse(ersiJson);
            Object esriFs = esriMap.get("features");
            if(esriFs instanceof List){
                esriFs = (List<Map<String, Object>>) esriFs;
                for(int i=0; i< ((List) esriFs).size(); i++){
                    Map esriF = (Map) ((List) esriFs).get(i);
                    Map geoF = new HashMap();
                    geoF.put("type", "Feature");
                    geoF.put("properties", esriF.get("attributes"));
                    Map<String, Object> geometry = (Map<String, Object>) esriF.get("geometry");
                    if(null != geometry.get("x")){
                        geoF.put("geometry", geoPoint(geometry));
                    }else if(null != geometry.get("points")){
                        geoF.put("geometry", geoPoints(geometry));
                    }else if(null != geometry.get("paths")){
                        geoF.put("geometry", geoLine(geometry));
                    }else if(null != geometry.get("rings")){
                        geoF.put("geometry", geoPoly(geometry));
                    }
                    geoFs.add(geoF);
                }
                geoMap.put("features", geoFs);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return new JSONObject(geoMap).toString();
    }
    //geoJosn转EsriJson
    public static String geo2ersi(String geoJson, String idAttribute){
        Map esriMap = new HashMap();
        try {
            Map geoMap = (Map) JSON.parse(geoJson);
            esriMap = getEsriGeo(geoMap, idAttribute);
            Map spatialReference = new HashMap();
            spatialReference.put("wkid", 4326);
            esriMap.put("spatialReference",spatialReference);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new JSONObject(esriMap).toString();
    }


    public static Map getEsriGeo(Map geoMap, String idAttribute){
        Map esriMap = new HashMap();
        idAttribute = (!idAttribute.isEmpty())? idAttribute:"OBJECTID";
        String type = geoMap.get("type").toString();
        switch (type){
            case "Point":
                List<BigDecimal> coords = (List<BigDecimal>) geoMap.get("coordinates");
                esriMap.put("x", coords.get(0));
                esriMap.put("y", coords.get(1));
                break;
            case "MultiPoint":
                esriMap.put("points",geoMap.get("coordinates"));
                break;
            case "LineString":
                List<Object> coordsList = new ArrayList<>();
                coordsList.add(geoMap.get("coordinates"));
                esriMap.put("paths",coordsList);
                break;
            case "MultiLineString":
                esriMap.put("paths",geoMap.get("coordinates"));
                break;
            case "Polygon":
                List<List<List<BigDecimal>>> coordinates = (List<List<List<BigDecimal>>>) geoMap.get("coordinates");
                List<List<List<BigDecimal>>> rings = orientRings(coordinates);
                esriMap.put("rings", rings);
                break;
            case "MultiPolygon":
                List<List<List<List<BigDecimal>>>> mcoordinates = (List<List<List<List<BigDecimal>>>>) geoMap.get("coordinates");
                List<List<List<BigDecimal>>> mrings = flattenMultiPolygonRings(mcoordinates);
                esriMap.put("rings", mrings);
                break;
            case "Feature":
                if (null != geoMap.get("geometry")) {
                    Map geometry = getEsriGeo((Map) geoMap.get("geometry"), idAttribute);
                    esriMap.put("geometry", geometry);
                }
                if(null != geoMap.get("properties")){
                    Map properties = (Map) geoMap.get("properties");
                    if (null != geoMap.get("id")) {
                        properties.put(idAttribute, geoMap.get("id"));
                    }
                    esriMap.put("attributes", properties);
                }
                break;
            case "FeatureCollection":
                List<Object> esriFs = new ArrayList<>();
                List<Map> features = (List<Map>) geoMap.get("features");
                for (int i = 0; i < features.size(); i++) {
                    esriFs.add(getEsriGeo(features.get(i), idAttribute));
                }
                esriMap.put("features", esriFs);
                esriMap.put("geometryType","esriGeometryPolygon");
                break;
            case "GeometryCollection":
                List<Object> esriFsc = new ArrayList<>();
                List<Map> geometries = (List<Map>) geoMap.get("geometries");
                for (int i = 0; i < geometries.size(); i++) {
                    esriFsc.add(getEsriGeo(geometries.get(i), idAttribute));
                }
                esriMap.put("geometries", esriFsc);
                esriMap.put("geometryType","esriGeometryPolygon");
                break;
        }
        return esriMap;
    }


    public static Map geoPoint(Map<String, Object> geometry){
        Map geo = new HashMap();
        geo.put("type", "point");
        BigDecimal x = (BigDecimal) geometry.get("x");
        BigDecimal y = (BigDecimal) geometry.get("y");
        List<BigDecimal> coords = new ArrayList<>();
        coords.add(x);
        coords.add(y);
        geo.put("coordinates", coords);
        return geo;
    }

    public static Map geoPoints(Map<String, Object> geometry){
        Map geo = new HashMap();
        List<Object> points = (List<Object>) geometry.get("points");
        if(points.size()==1){
            geo.put("type", "Point");
            geo.put("coordinates", points.get(0));
        }else{
            geo.put("type", "MultiPoint");
            geo.put("coordinates", points);
        }
        return geo;
    }

    public static Map geoLine(Map<String, Object> geometry){
        Map geo = new HashMap();
        List<Object> paths = (List<Object>) geometry.get("paths");
        if(paths.size()==1){
            geo.put("type", "LineString");
            geo.put("coordinates", paths.get(0));
        }else{
            geo.put("type", "MultiLineString");
            geo.put("coordinates", paths);
        }
        return geo;
    }

    public static Map geoPoly(Map<String, Object> geometry){
        Map geo = new HashMap();
        List<List<List<BigDecimal>>> rings = (List<List<List<BigDecimal>>>) geometry.get("rings");
        if(rings.size()==1){
            geo.put("type", "Polygon");
            geo.put("coordinates", rings);
        }else{
            List<List<List<List<BigDecimal>>>> coords = new ArrayList();
            String type = "";
            int len = coords.size() - 1;
            for(int i=0; i< rings.size(); i++){
                if(ringIsClockwise( rings.get(i))){
                    List<List<List<BigDecimal>>> item = new ArrayList<>();
                    item.add(rings.get(i));
                    coords.add(item);
                    len++;
                }else{
                    coords.get(len).add(rings.get(i));
                }
            }
            if(coords.size() == 1){
                type="Polygon";
            }else{
                type="MultiPolygon";
            }
            geo.put("type", type);
            geo.put("coordinates",coords.size()==1?coords.get(0): coords);
        }
        return geo;
    }

    public static boolean ringIsClockwise(List<List<BigDecimal>> rings){
        int total = 0;
        List<BigDecimal> pt1 = null;
        List<BigDecimal> pt2 = null;
        for(int i=0; i< rings.size()-1; i++){
            pt1 = rings.get(i);
            pt2 = rings.get(i+1);
            total += (pt2.get(0).doubleValue() - pt1.get(0).doubleValue())*  (pt2.get(1).doubleValue() + pt1.get(1).doubleValue());
        }
        return total>=0;
    }

    public static List<List<List<BigDecimal>>> orientRings ( List<List<List<BigDecimal>>> polygon) {
        List<List<List<BigDecimal>>> ringsList = new ArrayList<>();
        List<List<BigDecimal>> outerRing = closeRing(polygon.get(0));
        if (outerRing.size() >= 4) {
            if (!ringIsClockwise(outerRing)) {
                Collections.reverse(outerRing);
            }
            ringsList.add(outerRing);
            polygon.remove(0);
            for (int i = 0; i < polygon.size(); i++) {
                List<List<BigDecimal>> hole = closeRing(polygon.get(i));
                if (hole.size() >= 4) {
                    if (ringIsClockwise(hole)) {
                        Collections.reverse(hole);
                    }
                    ringsList.add(hole);
                }
            }
        }
        return ringsList;
    }

    public static List<List<BigDecimal>> closeRing (List<List<BigDecimal>> coords) {
        if (!pointsEqual(coords.get(0), coords.get(coords.size()-1))) {
            coords.add(coords.get(0));
        }
        return coords;
    }

    public static boolean pointsEqual (List<BigDecimal> a, List<BigDecimal> b) {
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i).compareTo(b.get(i)) != 0) {
                return false;
            }
        }
        return true;
    }

    public static List<List<List<BigDecimal>>> flattenMultiPolygonRings (List<List<List<List<BigDecimal>>>> rings) {
        List<List<List<BigDecimal>>> polygonList = new ArrayList<>();
        for (int i = 0; i < rings.size(); i++) {
            List<List<List<BigDecimal>>> polygons = orientRings(rings.get(i));
            for (int x = polygons.size() - 1; x >= 0; x--) {
                List<List<BigDecimal>> polygon = polygons.get(x);
                polygonList.add(polygon);
            }
        }
        return polygonList;
    }

}