package com.example.ia;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

    public static String getPath(Context context, Uri uri) {
        File file = null;
        try {
            // Crea un archivo temporal en el directorio de caché
            file = new File(context.getCacheDir(), queryName(context.getContentResolver(), uri));
            try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                 FileOutputStream outputStream = new FileOutputStream(file)) {
                int read;
                byte[] buffers = new byte[1024];  // Buffer de 1 KB para leer el archivo
                while ((read = inputStream.read(buffers)) != -1) {
                    outputStream.write(buffers, 0, read);  // Escribe en el archivo temporal
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;  // En caso de error al leer o escribir el archivo
            }
            return file.getPath();  // Devuelve la ruta del archivo
        } catch (Exception e) {
            e.printStackTrace();
            return null;  // Maneja cualquier otra excepción
        }
    }

    // Método para obtener el nombre del archivo desde el ContentResolver
    private static String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor = null;
        String displayName = null;
        try {
            returnCursor = resolver.query(uri, null, null, null, null);
            if (returnCursor != null && returnCursor.moveToFirst()) {
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                displayName = returnCursor.getString(nameIndex);
            }
        } finally {
            if (returnCursor != null) {
                returnCursor.close();  // Asegúrate de cerrar el cursor para evitar fugas de memoria
            }
        }
        return displayName != null ? displayName : "unknown_file";
    }
}
