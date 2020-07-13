package cn.treeh.generator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class LinkClusterandReads {
    private HashMap<String, HashSet<String>> cluster2reads;

    public HashMap<String, String> getCluster_clname2name() {
        return cluster_clname2name;
    }

    private HashMap<String, String> cluster_clname2name;
    //file1 name 2 clname
    //file2 readname 2 clname
    public LinkClusterandReads(String file1, String file2,String sep){
        try{
            cluster2reads = new HashMap<>();
            cluster_clname2name = new HashMap<>();
            BufferedReader reader = new BufferedReader(new FileReader(file1));
            String line;
            String[] splited;
            while((line = reader.readLine()) != null){
                splited = line.split("[\t]");
                if(splited.length != 2)
                    continue;
                cluster_clname2name.put(splited[1], splited[0]);
            }
            reader.close();
            reader = new BufferedReader(new FileReader(file2));
            String name;
            HashSet<String> tempset;
            while((line = reader.readLine()) != null){
                splited = line.split("[\t]");
                if(splited.length != 2)
                    continue;
                if((name = cluster_clname2name.getOrDefault(splited[1], null)) == null)
                    continue;
                tempset = cluster2reads.getOrDefault(name, null);
                if(tempset != null)
                    tempset.add(splited[0]);
                else {
                    tempset = new HashSet<>();
                    tempset.add(splited[0]);
                    cluster2reads.put(name, tempset);
                }
            }
            reader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public LinkClusterandReads(String file1, String sep) {
        try{
            BufferedReader reader = new BufferedReader(new FileReader(file1));
            String line;
            cluster2reads = new HashMap<>();
            reader.readLine();
            while((line = reader.readLine()) != null){
                String[] splited = line.split("[" + sep +"]");
                if(splited.length != 2)
                    continue;
                if(cluster2reads.containsKey(splited[0])) {
                    cluster2reads.get(splited[0]).add(splited[1]);
                }
                else{
                    HashSet<String> tmpset = new HashSet<>();
                    tmpset.add(splited[1]);
                    cluster2reads.put(splited[0], tmpset);
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void export(String file){
        try{
            FileWriter writer = new FileWriter(file);
            for(Map.Entry<String, HashSet<String>> e : cluster2reads.entrySet()){
                writer.write(e.getKey() + "\t" + e.getValue().size() + "\n");
                for(String r : e.getValue()){
                    writer.write(r + "\n");
                }
            }
            writer.flush();
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public HashMap<String, HashSet<String>> getCluster2reads(){
        return cluster2reads;
    }
}
