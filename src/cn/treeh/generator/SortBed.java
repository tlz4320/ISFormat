package cn.treeh.generator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class SortBed {

    static public class bedread implements Comparable<bedread>{
        public bedread(String read, int b, int e){
            beddata = read;
            begin = b;
            end = e;
        }
        String beddata;
        int begin;
        int end;
        @Override
        public int compareTo(bedread r){
            return r.begin - begin;
        }
    }
    static public class bin{
        public int begin;
        public int width;
        public bin(){
            begin = width = 0;
        }
        public bin(int b){
            begin = b;
            width = 0;
        }
        public bin(int b, int w){
            begin = b;
            width = w;
        }
    }
    static FileWriter writer;
    static FileWriter writer1;
    static private HashMap<String, HashSet<String>> clusterReads;
    public static void setClusterReads(HashMap<String, HashSet<String>> c){
        clusterReads = c;
    }
    static public void index(String path, String path2, String path3){

        HashMap<String, ArrayList<bedread>> sortedresult = sort(path, path3);
        int fileposition = 0;

        try {
            writer = new FileWriter(path2);
            writer1 = new FileWriter(path2 + ".bdx");
            ArrayList<bedread> readslist;
            for (Map.Entry<String, ArrayList<bedread>> e : sortedresult.entrySet()) {

                int maxlength = 0;
                readslist = e.getValue();
                for(bedread bed : readslist){
                    if(maxlength < bed.end)
                        maxlength = bed.end;
                }
                Collections.sort(readslist);
                int step = (maxlength / 14) + 1;
                LinkedList<bin> binlist = new LinkedList<>();
                fileposition = makebins(1, step, readslist, fileposition, maxlength, binlist);
                writer1.write(e.getKey()+"\t"+maxlength + "\t" + binlist.size() +"\n");

                for(bin bin2 : binlist){
                    if(bin2 == null)
                        writer1.write("0\t0\n");
                    else
                    writer1.write(bin2.begin + "\t" + bin2.width + "\n");
                }
            }
            writer.flush();
            writer1.flush();
            writer.close();
            writer1.close();
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    static public int makebins(int level, int stepwindow, ArrayList<bedread> remain, int nowfileposition, int maxlength, LinkedList<bin> binlist) throws Exception{
        int windowbegin = 0;
        int windowend = level * stepwindow;
        bedread read;
        while(windowend < (maxlength + stepwindow)){
            bin b = new bin(nowfileposition);
            for(int index = remain.size() - 1; index > -1; index--){
                read = remain.get(index);
                if(read.begin > windowend)
                    break;
                if(read.begin > windowbegin && read.end < windowend){
                    if(!clusterReads.containsKey(getClustername(read.beddata))){
                        remain.remove(index);
                        continue;
                    }
                    writer.write(read.beddata+"\n");
                    HashSet<String> set = clusterReads.get(getClustername(read.beddata));
                    int lengthacc = 0;
                    for(String r : set) {
                        writer.write(r + "\n");
                        lengthacc += r.length() + 1;
                    }
                    b.width += (read.beddata.length() + 1 + lengthacc);
                    nowfileposition += (read.beddata.length() + 1 + lengthacc);
                    remain.remove(index);
                }

            }
            binlist.add(b);
            windowbegin += stepwindow;
            windowend += stepwindow;
        }
        if(remain.size() != 0)
            return makebins(level + 1, stepwindow, remain, nowfileposition, maxlength, binlist);
        return nowfileposition;
    }
    public static String getClustername(String bed){
        return bed.split("[\t]")[3];
    }
    static public HashMap<String, ArrayList<bedread>> sort(String path, String path3){
        HashMap<String, ArrayList<bedread>> res = new HashMap<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            while((line = reader.readLine()) != null){
                if(line.length() < 2)
                    continue;
                String temp[] = line.split("[\t]");
                if(temp.length < 3)
                    continue;
                String chr = temp[0];
                int begin = Integer.parseInt(temp[1]);
                int end = Integer.parseInt(temp[2]);
                if(res.containsKey(chr))
                    res.get(chr).add(new bedread(line, begin, end));
                else{
                    ArrayList<bedread> newlist = new ArrayList<>();
                    newlist.add(new bedread(line, begin, end));
                    res.put(chr, newlist);
                }
            }
            reader.close();
            if(clusterReads == null) {
                clusterReads = new HashMap<>();
                reader = new BufferedReader(new FileReader(path3));
                while ((line = reader.readLine()) != null) {
                    String[] temp = line.split("[\t]");
                    if (temp.length != 2)
                        continue;
                    int readsnum = Integer.parseInt(temp[1]);
                    HashSet<String> set = new HashSet<>();
                    for (int i = 0; i < readsnum; i++) {
                        line = reader.readLine();
                        if (line == null || line.length() < 2)
                            break;
                        set.add(line);
                    }
                    if (set.size() != 0)
                        clusterReads.put(temp[0], set);
                }
            }
        }catch (Exception e){
            throw  new RuntimeException(e);
        }
        return res;
    }
    static public class Bedbins{
        int maxlength;
        int step;
        bin[] bins = new bin[106];
    }
    static public class BedIndex{

        HashMap<String, Bedbins> index;
        public BedIndex(){
            index = new HashMap<>();
        }
        public HashSet<bin> getRegin(String chr, int b, int e){
            HashSet<bin> res = new HashSet<>();
            if(!index.containsKey(chr))
                return res;
            Bedbins bedbins = index.get(chr);
            int beginbinindex = b / bedbins.step < 14 ? b / bedbins.step:14;
            int endbinindex = e/bedbins.step < 14 ? e/bedbins.step : 13;
            int shift = 0;
            for(int i = 14; i > 0 && beginbinindex <= endbinindex; i--, endbinindex--){
                bin libin = new bin(bedbins.bins[shift + beginbinindex].begin);
                for(int index = beginbinindex; index <= endbinindex; index++)
                    libin.width += bedbins.bins[index + shift].width;
                shift += i;
                if(libin.width != 0)
                    res.add(libin);
            }
            return res;
        }
    }
    static public BedIndex readIndex(String path){
        BedIndex index = new BedIndex();
        try{
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            while((line = reader.readLine()) != null){
                if(line.length() < 2)
                    continue;
                String[] splited = line.split("[\t]");
                Bedbins bedbins = new Bedbins();
                index.index.put(splited[0], bedbins);
                bedbins.maxlength = Integer.parseInt(splited[1]);
                bedbins.step = bedbins.maxlength / 14 + 1;
                int binlistsize = Integer.parseInt(splited[2]);
                for(int i = 0; i < binlistsize; i++){
                    line = reader.readLine();
                    if(line == null || line.length() < 2)
                        break;
                    splited = line.split("[\t]");
                    bedbins.bins[i] = new bin(Integer.parseInt(splited[0]), Integer.parseInt(splited[1]));
                }
                for(;binlistsize <= 105; binlistsize++)bedbins.bins[binlistsize] = new bin();
            }


        }catch (Exception e) {
            e.printStackTrace();
        }
        return index;
    }
}
