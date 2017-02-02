package net.syscon.util;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.EnumerablePropertySource;


public class ReloadablePropertySource extends EnumerablePropertySource<Map<String, String>> implements Runnable {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Optional<WatchService> watcherService = Optional.empty();
	private Optional<Thread> watchingThread = Optional.empty();
	private boolean watching = false;
	private final File propertyFile;
	
	public ReloadablePropertySource(String name, File file) {
		super(name, new ConcurrentHashMap<String, String>());
		this.propertyFile = file;
		loadConfigFile();
	}
	
    public void start() {
        if (watchingThread.isPresent()) {
            logger.info("The file \"{}\" is already being watched!", propertyFile.getName());
        } else {
            logger.info("Starting to watch the file \"{}\" ...", propertyFile.getName());
            watchingThread = Optional.of(new Thread(this));
            watchingThread.get().start();
        }
    }

    public void stop() {
        if (watchingThread.isPresent()) {
            logger.info("Stopping to watch the file \"{}\" ...", propertyFile.getName());
            try {
                watching = false;
                if (watcherService.isPresent()) {
                    watcherService.get().close();
                }
                watchingThread.get().interrupt();
            } catch (final IOException e) {
            }
            watchingThread = Optional.empty();
        }
    }


    @Override
    public void run() {

        try {

            // stop monitoring thread if the application stops
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    watching = false;
                }
            });

            final Path path = Paths.get(propertyFile.getParent());
            watcherService = Optional.of(path.getFileSystem().newWatchService());
            path.register(watcherService.get(), ENTRY_MODIFY);
            watching = true;

            logger.info("Monitoring the file \"{}\" ...", propertyFile.getAbsolutePath());
            while (watching) {
                final WatchKey key = watcherService.get().take();
                // verify all directory change events
                for (final WatchEvent<?> e : key.pollEvents()) {
                    if (e.context().toString().equalsIgnoreCase(propertyFile.getName())) {
                        logger.info("The file \"{}\" was changed, reloading ...", propertyFile.getAbsolutePath());
                        loadConfigFile();
                    }
                }
                key.reset();
            }
        } catch (final Exception e) {
            logger.info(e.getMessage());
        }
        logger.info("Watching the file \"{}\" was stopped!", propertyFile.getAbsolutePath());
    }

	
    @Override
    public String[] getPropertyNames() {
    	return source.keySet().stream()
    		.map(key -> key.toString())
    		.collect(Collectors.toList())
    		.toArray(new String[0]);
    }
    
    public void loadConfigFile() {
    	if (propertyFile.exists()) {
    		try (InputStream in = new FileInputStream(propertyFile)) {
    			final Properties tmp = new Properties();
    			tmp.load(in);
    			// add/update keys ...
    			for (final Object key: tmp.keySet()) {
    				source.put(key.toString(), tmp.getProperty(key.toString()));
    			}
    			// delete the removed keys ...
    			for (final String key: source.keySet()) {
    				if (!tmp.containsKey(key)) {
    					source.remove(key);
    				}
    			}
    		} catch (final IOException ex) {
    		    logger.error(ex.getMessage(), ex);
    		}
    	} else {
    		source.clear();
    	}
    }


    @Override
    public Object getProperty(String name) {
        String result = "";
        if (source.containsKey(name)) {
            result = source.get(name).trim();
        } else {
            logger.warn(MessageFormat.format("Property \"{0}\" does not exists on the file {1}", name, propertyFile.getAbsolutePath()));
        }
        return result;
    }


}
