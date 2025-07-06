package ru.yandex.practicum.taskmanagerapp.httpserver;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeTypeAdapter extends TypeAdapter<LocalDateTime> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.uuuu HH:mm:ss");

    @Override
    public void write(final JsonWriter jsonWriter, final LocalDateTime date) throws IOException {
        jsonWriter.value((date == null) ? null : date.format(formatter));
    }

    @Override
    public LocalDateTime read(final JsonReader jsonReader) throws IOException {
        return LocalDateTime.parse(jsonReader.nextString(), formatter);
    }
}
