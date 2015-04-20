package filesearching2;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//File Search with ForkJoin
//https://docs.oracle.com/javase/tutorial/essential/concurrency/forkjoin.html        
// D:\\Downloads\\1\\1  I:\\Series\\1\\1
//Music library folders:   D:\Music library  D:\TMP Music  D:\Downloads\Music // .mp3

public class FileSearching2 {

    public static void main(String[] args) {                
        ForkJoinPool pool = new ForkJoinPool();
        long t0 = System.nanoTime();
        //Stream<File> result = pool.invoke(new DirectoryWalker(new File("D:\\Music library"), ""));
        Stream<File> result = pool.invoke(new DirectoryWalker(new File("D:\\Music library\\"), ""));
        result.forEach(System.out::println);
        long t1 = System.nanoTime();
        
//        long count = 0;
//        long fileCount = 0;
//        long dirCount = 0;
//        List<File> list = new ArrayList<>();
//                
//        result.forEach(list::add);
//        for(File f:list){
//            //System.out.println(""+f.getName());
//            if (f.isDirectory()) { dirCount++;}
//            else {
//                fileCount++;
//            }
//            count++;
//        }
//        System.out.println("All count is "+count+". It's "+fileCount+" file(s), "+dirCount+" folder(s).");
//        //>1MByte
//        // List<File> list
//        List l = list.stream().filter(f -> f.length()>10).collect(Collectors.toCollection(ArrayList::new));
//        System.out.println(""+l.size());        
        
        
        long t2 = System.nanoTime();        
//        Stream<File> result2 = pool.invoke(new DirectoryWalker(new File("D:\\TMP Music"), ""));                
//        result2.forEach(System.out::println);        
        long t3 = System.nanoTime();
        
        long millis1 = TimeUnit.NANOSECONDS.toMillis(t1 - t0);
//        long millis2 = TimeUnit.NANOSECONDS.toMillis(t2 - t1);
        long millis3 = TimeUnit.NANOSECONDS.toMillis(t3 - t2);
        System.out.println(String.format("time 1: %d ms, time 2: %d ms", millis1, millis3));
//        System.out.println(String.format("время между форкджойнами = %d ms", millis2));
    }
}

class DirectoryWalker extends RecursiveTask<Stream<File>> {

    private final File dir;
    private final String name;

    public DirectoryWalker(File dir, String name) {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Это не папка, але!");
        }
        this.name = name;
        this.dir = dir;
    }

    @Override
    protected Stream<File> compute() {
        //System.out.println("Смотрим в папку " + dir);//TODO убрать потом
        List<File> all = toList(dir.listFiles());

        List<ForkJoinTask<Stream<File>>> tasks = new LinkedList<>();
        tasks.add(new FileLooker(all.stream(), name).fork());

        Stream<File> dirs = all.stream().filter(f -> f.isDirectory());
        Stream<ForkJoinTask<Stream<File>>> dirTasks = dirs.map(subdir -> new DirectoryWalker(subdir, name).fork());
        dirTasks.forEach(tasks::add);

        return tasks.stream().flatMap(t -> t.join());
    }

    private static <E> List<E> toList(E[] a) {
        if (a == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(a);
        }
    }
}

class FileLooker extends RecursiveTask<Stream<File>> {

    private final Stream<File> files;
    private final String name;

    public FileLooker(Stream<File> files, String name) {
        this.files = files;
        this.name = name;
    }

    @Override
    protected Stream<File> compute() {
        return files.filter((File f) -> f.getName().contains(name));
    }

}
