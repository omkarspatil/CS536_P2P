package entity;

import java.net.InetAddress;
import java.util.*;

public class FileIndex {
    Map<String, Set<InetAddress>> index;

    public FileIndex(){
        index = new HashMap<>();
    }

    public void add(String file, InetAddress host){
        index.putIfAbsent(file, new TreeSet<>(Collections.reverseOrder()));
        index.get(file).add(host);
    }

    public Set<InetAddress> get(String file){
        return index.containsKey(file) ? index.get(file) : null;
    }

    public void remove(String file, InetAddress host){
        if(index.containsKey(file)){
            index.get(file).remove(host);
        }
    }

    public Set<String> getFiles(){
        Set<String> toReturn  = new TreeSet<>(Collections.reverseOrder());
        toReturn.addAll(index.keySet());
        return toReturn;
    }

    public Map<String, Set<InetAddress>> getHostsMap(Set<String> files){
        Map<String, Set<InetAddress>> map = new HashMap<>();
        for(String file: files){
            if(index.containsKey(file))
            map.put(file,index.get(file));
        }
        return map;
    }

    @Override
    public String toString() {
        return index.toString();
    }
}
