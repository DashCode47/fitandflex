package com.backoffice.fitandflex;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FitandflexApplication {

	public static void main(String[] args) {
		// Cargar variables de entorno desde .env si existe
		loadEnvVariables();
		SpringApplication.run(FitandflexApplication.class, args);
	}

	private static void loadEnvVariables() {
		try {
			java.nio.file.Path envPath = java.nio.file.Paths.get(".env");
			if (java.nio.file.Files.exists(envPath)) {
				java.util.List<String> lines = java.nio.file.Files.readAllLines(envPath);
				for (String line : lines) {
					if (line != null && !line.trim().isEmpty() && !line.startsWith("#") && line.contains("=")) {
						String[] parts = line.split("=", 2);
						String key = parts[0].trim();
						String value = parts[1].trim();
						if (System.getenv(key) == null && System.getProperty(key) == null) {
							System.setProperty(key, value);
						}
					}
				}
				// System.out.println("Loaded .env file successfully");
			}
		} catch (Exception e) {
			System.err.println("Could not load .env file: " + e.getMessage());
		}
	}

}
