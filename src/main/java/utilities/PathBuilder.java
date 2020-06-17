/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.net.URI;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

/// toString() always returns a file path.
public class PathBuilder {
    public static final String FILE_PREFIX = "file:";
    public static final String URI_SEPARATOR = "/"; // This is the only acceptable separator for URI
    public static final String EMPTY_SPACE = "%20"; // URI cannot contain empty space
    
    private @NotNull String _value;
    private final @NotNull String _separator;
    
    public PathBuilder(@NotNull String path) {
        _value = path;
        _separator = FileSystem.getDirectorySeparator();
    }
    
    public PathBuilder(@NotNull URI path) {
        String uriPath = path.toString();
        _value = uriPath != null ? uriPath : "";
        _separator = FileSystem.getDirectorySeparator();
    }
    
    public PathBuilder(@NotNull Path path) {
        _value = path.getPath();
        _separator = path.getSeparator();
    }
    
    @Override
    public String toString() {
        String path = _value;
        
        // 1. Remove file prefix
        path = path.replace(FILE_PREFIX, "");
        
        // 2. This is the only acceptable separator for URI
        path = path.replace("/", _separator);
        
        // 3. Replace empty spaces with the appropriate character
        path = path.replace(EMPTY_SPACE, " ");
        
        return path;
    }
    
    public void appendComponent(@NotNull String part) {
        String separator = _value.endsWith(_separator) ? "" : _separator;
        _value = _value.concat(separator + part);
    }
    
    public void removeLastComponent() {
        String path = _value;
        
        if (path.isEmpty()) {
            return;
        }
        
        String result = "";
        
        String[] segments = path.split(Pattern.quote(_separator));
        
        for (int e = 0; e < segments.length-1; e++) {
            if (e < segments.length-2) {
                result = result.concat(segments[e] + _separator);
            } else {
                // Do not add separator for last component
                result = result.concat(segments[e]);
            }
        }
        
        _value = result;
    }
    
    public @NotNull Path get() {
        try {
            URI uri = PathBuilder.convertFilePathToURI(_value);
            
            return new GenericPath(uri);
        } catch (Exception e) {
            return new GenericPath(URI.create(""));
        }
    }
    
    public @NotNull String getLastComponent() {
        if (_value.isEmpty()) {
            return "";
        }
        
        String[] segments = _value.split(Pattern.quote(_separator));
        return segments[segments.length-1];
    }
    
    // # Static helpers
    
    public static String convertURIToFilePath(@NotNull URI uri) {
        return new PathBuilder(uri).toString();
    }
    
    public static URI convertFilePathToURI(@NotNull String path) {
        String result = path;
        
        // 1. Insert the file: prefix, required for URI
        if (!result.startsWith(FILE_PREFIX)) {
            result = FILE_PREFIX.concat(result);
        }
        
        // 2. This is the only acceptable separator for URI
        result = result.replace("\\", URI_SEPARATOR);
        
        // 3. Replace empty spaces with the appropriate character
        result = result.replace(" ", EMPTY_SPACE);
        
        return URI.create(result);
    }
}

class GenericPath implements Path {
    private URI _url;
    
    GenericPath(@NotNull URI path) {
        _url = path;
    }
    
    @Override
    public String toString() {
        return getPath();
    }
    
    @Override
    public URI getURL() {
        return _url;
    }
    
    @Override
    public String getPath() {
        String path = _url.getPath();
        return path != null ? path : "";
    }

    @Override
    public String getLastComponent() {
        String path = _url.getPath();
        
        if (path == null) {
            return "";
        }
        
        String[] segments = path.split(Pattern.quote(PathBuilder.URI_SEPARATOR));
        return segments[segments.length-1];
    }

    @Override
    public String getSeparator() {
        return PathBuilder.URI_SEPARATOR;
    }
}
