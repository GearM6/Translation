 import com.google.auth.oauth2.ServiceAccountCredentials;
 import com.google.cloud.translate.*;

 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;

 public class Translation {
     public static StringBuilder translatedText = new StringBuilder();
     public static PriorityQueue<TranslatedText> strings = new PriorityQueue<>();
     static Lock lock = new ReentrantLock();

     public static class TranslatedText implements Comparable<TranslatedText>{
         public int order;
         public String text;

        public TranslatedText(int order, String text){
            this.order = order;
            this.text = text;
        }

         @Override
         public int compareTo(TranslatedText o) {
             if(this.order > o.order){
                 return 1;
             }
             else {
                 return -1;
             }
         }
     }
     public static class TranslatorThread extends Thread{
         String text;
         String translatedText;
         private int order;

         public TranslatorThread(String text, int order){
             this.order = order;
             this.text = text;

         }
         @Override
         public void run(){
             Translate translate;
             try {
                 translate = TranslateOptions.newBuilder().setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream("./translator-1583097921990-cc6687b10245.json"))).build().getService();
                 com.google.cloud.translate.Translation translation = translate.translate(this.text, Translate.TranslateOption.sourceLanguage("en"), Translate.TranslateOption.targetLanguage("fr"));
                 this.translatedText = translation.getTranslatedText();
                 lock.lock();
                    strings.add(new TranslatedText(this.order, this.translatedText));
                 lock.unlock();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
     public static void main(String[] args) throws IOException {
         File file = new File("./doi.txt");
         BufferedReader reader = new BufferedReader(new FileReader(file));
         int chunks = 0;
         StringBuilder paragraphBuilder = new StringBuilder();
         String line;
         List<TranslatorThread> threadList = new ArrayList<>();
         PriorityQueue textOrderQueue = new PriorityQueue();
         long startTime = System.nanoTime();
         //uncomment this for multithreading
//         while((line = reader.readLine()) != null){
//             if(!line.equals("")){
//                 paragraphBuilder.append(line);
//             }
//             else {
//                 TranslatorThread translatorThread = new TranslatorThread(paragraphBuilder.toString(), ++chunks);
//                 translatorThread.start();
//                 threadList.add(translatorThread);
//                 paragraphBuilder = new StringBuilder();
//             }
//         }

         //comment this for multithreading
         while((line = reader.readLine()) != null) {
             paragraphBuilder.append(line);
         }

         //end of comment section
         TranslatorThread translatorThread = new TranslatorThread(paragraphBuilder.toString(), ++chunks);
         translatorThread.start();
         threadList.add(translatorThread);

         for(TranslatorThread thread : threadList){
             try {
                 thread.join();
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }

         while(!strings.isEmpty()){
             translatedText.append(strings.poll().text +  "\n");
         }
         File translatedFile = new File("./TranslatedDeclarationOfIndependence.txt");
         BufferedWriter writer = new BufferedWriter(new FileWriter(translatedFile));
         writer.write(translatedText.toString());
         writer.close();
         long endTime = System.nanoTime();
         System.out.println("Elapsed time: "+  (endTime - startTime)/Math.pow(10,9) + " seconds");
    }
 }
