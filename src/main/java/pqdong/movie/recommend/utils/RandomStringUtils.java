package pqdong.movie.recommend.utils;

import java.util.Random;

public class RandomStringUtils {

    // 可选字符集（大小写字母 + 数字）
    private static final String CHARACTERS = 
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    
    private static final Random RANDOM = new Random();

    // 生成随机字符串（默认字符集）
    public static String generate() {
        return generate(3, 5, CHARACTERS);
    }

    // 生成随机字符串（自定义字符集）
    public static String generate(String characters) {
        return generate(3, 5, characters);
    }

    // 核心生成方法
    public static String generate(int minLength, int maxLength, String characters) {
        if (minLength < 1 || maxLength < minLength) {
            throw new IllegalArgumentException("Invalid length range");
        }
        
        int length = RANDOM.nextInt(maxLength - minLength + 1) + minLength;
        StringBuilder sb = new StringBuilder(length);
        
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }

    // 示例用法
    public static void main(String[] args) {
        System.out.println("Default: " + generate());
        System.out.println("Numbers: " + generate("0123456789"));
        System.out.println("Custom: " + generate(3, 5, "abcXYZ!@#"));
    }
}