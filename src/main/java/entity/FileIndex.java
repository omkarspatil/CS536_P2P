package entity;

import java.net.InetAddress;
import java.util.*;

public class FileIndex {
    Map<String, Set<InetAddress>> index;

    public FileIndex(){
        index = new HashMap<>();
    }

    public void add(String file, InetAddress host){
        index.putIfAbsent(file, new HashSet<>());
        index.get(file).add(host);
    }

    public void remove(String file, InetAddress host){
        if(index.containsKey(file)){
            index.get(file).remove(host);
        }
    }

    @Override
    public String toString() {
        return index.toString();
    }
}
