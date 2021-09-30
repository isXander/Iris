package net.coderbot.iris.shaderpack;

import net.coderbot.iris.Iris;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class LanguageMap {
	private final Map<String, Map<String, String>> translationMaps;

	public LanguageMap(Path root) throws IOException {
		this.translationMaps = new HashMap<>();

		if (!Files.exists(root)) {
			return;
		}

		// We are using a max depth of one to ensure we only get the surface level *files* without going deeper
		// we also want to avoid any directories while filtering
		// Basically, we want the immediate files nested in the path for the langFolder
		// There is also Files.list which can be used for similar behavior
		Files.walk(root, 1).filter(path -> !Files.isDirectory(path)).forEach(path -> {

			Map<String, String> currentLanguageMap = new HashMap<>();
			// Shader packs use legacy file name coding which is different than modern minecraft's.
			// An example of this is using "en_US.lang" compared to "en_us.json"
			// Also note that OptiFine uses a property scheme for loading language entries to keep parity with other
			// OptiFine features
			String currentFileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
			String currentLangCode = currentFileName.substring(0, currentFileName.lastIndexOf("."));
			Properties properties = new Properties();

			try {
				// Use InputStreamReader to avoid the default charset of ISO-8859-1.
				// This is needed since shader language files are specified to be in UTF-8.
				properties.load(new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8));
			} catch (IOException e) {
				Iris.logger.error("Failed to parse shader pack language file " + path, e);
			}

			properties.forEach((key, value) -> currentLanguageMap.put(key.toString(), value.toString()));
			translationMaps.put(currentLangCode, currentLanguageMap);
		});
	}

	public Map<String, String> getTranslations(String language) {
		return translationMaps.get(language);
	}
}
