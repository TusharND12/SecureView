# SecureView Desktop Application

Java-based desktop application for face recognition authentication and intrusion detection.

## Building

```bash
mvn clean package
```

## Running

```bash
java -jar target/secureview-desktop-1.0.0.jar
```

## Configuration

Configuration file location: `~/.secureview/config.json`

See main README.md for configuration details.

## Dependencies

- OpenCV 4.8.0+
- Firebase Admin SDK 9.2.0+
- Java 11+

## Project Structure

```
src/main/java/com/secureview/desktop/
├── SecureViewApplication.java      # Main entry point
├── AuthenticationWindow.java        # Authentication UI
├── RegistrationWindow.java          # Registration UI
├── config/                          # Configuration management
├── face/                            # Face recognition modules
│   ├── detection/                   # Face detection
│   ├── embedding/                   # Embedding extraction
│   └── liveness/                    # Liveness detection
├── encryption/                      # Encryption service
├── firebase/                        # Firebase integration
├── lock/                            # System locking
├── logging/                         # Attempt logging
└── autostart/                       # Auto-start functionality
```

## Development

### Adding New Features

1. Create new module in appropriate package
2. Register in `SecureViewApplication`
3. Update configuration if needed
4. Add tests

### Testing

```bash
mvn test
```

## Troubleshooting

See main README.md troubleshooting section.

