package pqdong.movie.recommend.utils;

public class ThreadLocalUtils {
   private static ThreadLocal<Long> threadLocal= new ThreadLocal<>();
   
   public static void setCurrentId(Long id){
       threadLocal.set(id);
   }
   
   public static Long getCurrentId(){
       return threadLocal.get();
   }

//    public static void deleteCurrentId(){
//        threadLocal.remove();
//    }
}