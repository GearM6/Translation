 import com.google.cloud.translate.*;


 public class Translation {
     static Translate translate = TranslateOptions.getDefaultInstance().getService();

     public static void main(String[] args) {
        com.google.cloud.translate.Translation translation = translate.translate("¡Hola Mundo!");
        System.out.printf("Translated Text:\n\t%s\n", translation.getTranslatedText());
    }
 }
