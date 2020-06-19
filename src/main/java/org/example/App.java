package org.example;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 */
public class App {

    /***
     * 获取一个项目下所有Controller的映射路径
     * @param args
     */
    public static void main(String[] args) {
        String sourcePath = "D:\\IdeaProjects\\TMS\\tcenter-api\\target\\classes";
        String resultPath = "D:\\controllerMappingTxt\\tcenter-api.txt";

        List<File> controllerList = new ArrayList<>();
        //获取所有的Controller文件
        getControllerList(new File(sourcePath), controllerList);

        getMapping(controllerList, sourcePath, resultPath);
    }

    private static void getMapping(List<File> controllerList, String classPath, String resultPath) {
        URL urlPath = null;
        try {
            urlPath = new URL("file://" + classPath.replaceAll("\\\\\\\\", "/") + "/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        ClassLoader consumer = new URLClassLoader(new URL[]{urlPath}, systemClassLoader);

        File resultFile = new File(resultPath);
        if (!resultFile.exists()) {
            try {
                resultFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        OutputStream out = null;
        try {
            out = new FileOutputStream(resultFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (File f : controllerList) {
            String fileName = f.getAbsolutePath();
            String className = fileName.substring(fileName.indexOf("com")).replaceAll("\\\\", "\\.").replaceAll("\\.class", "");
            try {
                Class<?> aClass = consumer.loadClass(className);
                StringBuilder mappingUrl = findMappingUrl(aClass);
                out.write(mappingUrl.toString().getBytes());
                out.flush();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static StringBuilder findMappingUrl(Class<?> clazz) {
        StringBuilder mappingStr = new StringBuilder();
        if (clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(RestController.class)) {
            String rootMapping = "";
            if (clazz.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = clazz.getDeclaredAnnotation(RequestMapping.class);
                rootMapping = requestMapping.value()[0];
            }

            mappingStr.append(clazz.getName()).append("\n");

            Method[] methods = clazz.getMethods();
            for (Method m : methods) {
                if (m.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping mapping = m.getDeclaredAnnotation(RequestMapping.class);
                    mappingStr.append((rootMapping + "/" + mapping.value()[0]).replaceAll("//+", "/")).append("\n");
                } else if (m.isAnnotationPresent(GetMapping.class)) {
                    GetMapping mapping = m.getDeclaredAnnotation(GetMapping.class);
                    mappingStr.append((rootMapping + "/" + mapping.value()[0]).replaceAll("//+", "/")).append("\n");
                } else if (m.isAnnotationPresent(PostMapping.class)) {
                    PostMapping mapping = m.getDeclaredAnnotation(PostMapping.class);
                    mappingStr.append((rootMapping + "/" + mapping.value()[0]).replaceAll("//+", "/")).append("\n");
                }
            }
        }
        return mappingStr;
    }

    /***
     * 获取所有的Controller文件,以Controller.class结尾
     * @param file
     * @param controllerList
     */
    private static void getControllerList(File file, List<File> controllerList) {
        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                getControllerList(f, controllerList);
            } else {
                String fileName = f.getName();
                if (fileName.endsWith("Controller.class")) {
                    controllerList.add(f);
                }
            }
        }
    }
}
