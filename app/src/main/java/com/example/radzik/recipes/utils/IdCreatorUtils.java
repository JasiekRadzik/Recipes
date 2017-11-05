package com.example.radzik.recipes.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Radzik on 21.08.2017.
 */

public class IdCreatorUtils {

        private static IdCreatorUtils mInstance;
        private static final Integer LENGTH = 10;
        private List<String> mIDList = new ArrayList<>();

        private IdCreatorUtils(){
        }

        public static synchronized IdCreatorUtils getInstance() {
            if(mInstance == null)
                mInstance = new IdCreatorUtils();

            return mInstance;
        }

        private String generateId() {
            Random random = new Random();

            // 83 znaki
            char[] values = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '-', '=', '+', '[', ']', '{', '}', '<', '>', '?', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
            String out = "";

            for(int i = 0; i < LENGTH; i++) {
                int x = random.nextInt(values.length);
                out = out + values[x];
            }

            return out;
        }

        public String getID() {
            Boolean isOriginalID = false;
            String id = "";

            while(isOriginalID == false) {
                id = generateId();
                if(!mIDList.contains(id))
                    isOriginalID = true;

            }
            return id;
        }
}
