package dev.ashhhleyyy.playerpronouns.impl.data;

import dev.ashhhleyyy.playerpronouns.api.PronounsApi;

import java.io.IOException;

public interface PronounDatabase extends PronounsApi.PronounReader, PronounsApi.ProunounWriter {

    void save() throws IOException;
}
