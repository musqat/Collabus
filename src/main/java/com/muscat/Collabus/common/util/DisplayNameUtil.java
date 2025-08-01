package com.muscat.Collabus.common.util;

import java.util.Random;
import java.util.function.Predicate;
import org.springframework.stereotype.Component;

@Component
public class DisplayNameUtil {
  public static String generateUniqueTag(String nickname, Predicate<String> isDuplicate) {
    String tag;
    String displayName;
    do {
      tag = String.format("%04d", new Random().nextInt(10000));
      displayName = nickname + "#" + tag;
    } while (isDuplicate.test(displayName));
    return tag;
  }
}
