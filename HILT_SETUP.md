# Hilt Dependency Injection Setup

This project uses **Hilt** for clean dependency injection, replacing manual factory patterns and repository creation.

## What is Hilt?

Hilt is Dagger's official dependency injection library for Android. It:
- Reduces boilerplate code
- Manages object lifecycles automatically
- Makes testing easier
- Provides scope-aware dependencies (Singleton, Activity, etc.)

## How It's Set Up

### 1. PaisaApp (Application Class)

```kotlin
@HiltAndroidApp
class PaisaApp : Application()
```

- **@HiltAndroidApp** initializes Hilt and creates the dependency container
- Must extend `Application`
- Declared in `AndroidManifest.xml` as `android:name=".PaisaApp"`

### 2. AppModule (Dependency Definitions)

Located at: `di/AppModule.kt`

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(...)
    }

    @Singleton
    @Provides
    fun provideTransactionRepository(database: AppDatabase): TransactionRepository {
        return TransactionRepository(...)
    }
}
```

- **@Module** marks this as a Hilt dependency module
- **@InstallIn(SingletonComponent::class)** makes dependencies available app-wide
- **@Provides** tells Hilt how to create instances
- **@Singleton** means only one instance exists for the entire app lifetime

### 3. MainActivity (Injection Point)

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var repository: TransactionRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // repository is automatically injected!
        
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            // ...
        }
    }
}
```

- **@AndroidEntryPoint** marks this Activity as an injection point
- **@Inject lateinit var repository** requests the dependency
- Hilt automatically provides the instance from AppModule

### 4. MainViewModel (ViewModel Injection)

```kotlin
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {
    // ...
}
```

- **@HiltViewModel** marks this as a Hilt-managed ViewModel
- Constructor injection of dependencies
- Used with `hiltViewModel()` in Compose:

```kotlin
val viewModel: MainViewModel = hiltViewModel()
```

## Dependency Flow

```
┌─────────────────────┐
│    PaisaApp         │
│  @HiltAndroidApp    │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────────────┐
│    Hilt Container           │
│   (manages dependencies)    │
└──────────┬──────────────────┘
           │
           ├─→ AppDatabase instance (Singleton)
           │
           ├─→ TransactionRepository instance
           │   (created from AppDatabase)
           │
           └─→ MainViewModel instance
               (created with repository)
                │
                └─→ Injected into MainActivity
```

## How to Extend

### Add a new dependency

1. **Create the provider function in AppModule**:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Singleton
    @Provides
    fun provideYourService(): YourService {
        return YourService()
    }
}
```

2. **Inject it into your class**:

```kotlin
@AndroidEntryPoint
class MyActivity : ComponentActivity() {
    @Inject
    lateinit var yourService: YourService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // yourService is ready to use
    }
}
```

### Inject into non-Activity classes

```kotlin
class MyRepository @Inject constructor(
    private val database: AppDatabase,
    private val anotherService: AnotherService
) {
    // Constructor injection
}
```

### Different scopes

```kotlin
// Activity-scoped (new instance per Activity)
@ActivityScoped
@Provides
fun provideActivityService(): ActivityService = ActivityService()

// Fragment-scoped
@FragmentScoped
@Provides
fun provideFragmentService(): FragmentService = FragmentService()
```

## Benefits in This Project

### Before (Manual Factory)
```kotlin
class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        val db = AppDatabase.getDatabase(this)
        val repository = TransactionRepository(db.dao1, db.dao2)
        viewModel = ViewModelProvider(this, MainViewModelFactory(repository))
            .get(MainViewModel::class.java)
    }
}
```

### After (With Hilt)
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var repository: TransactionRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
        }
    }
}
```

## Testing with Hilt

### Unit tests
```kotlin
@UninstallModules(AppModule::class)
@HiltAndroidTest
class MainViewModelTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var repository: TransactionRepository
    
    @Test
    fun testViewModel() {
        hiltRule.inject()
        // Test with injected repository
    }
}
```

### Provide test implementations
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {
    
    @Singleton
    @Provides
    fun provideTestRepository(): TransactionRepository {
        return FakeTransactionRepository() // Test double
    }
}
```

## Common Patterns

### Inject into ViewModel
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel()
```

### Inject into Repository
```kotlin
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao
)
```

### Inject into BroadcastReceiver (if needed)
```kotlin
@HiltAndroidApp
class PaisaApp : Application()

// For receivers, use EntryPointAccessors:
class MySmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            MyEntryPoint::class.java
        )
        val repository = entryPoint.repository()
        // Use repository
    }
}
```

## File Structure

```
app/src/main/java/com/example/expensetracker/
├── PaisaApp.kt                  # @HiltAndroidApp
├── MainActivity.kt              # @AndroidEntryPoint
├── di/
│   └── AppModule.kt             # @Module with @Provides
├── data/
│   ├── AppDatabase.kt
│   ├── Transaction.kt
│   └── Repository.kt
└── ui/
    ├── MainViewModel.kt         # @HiltViewModel
    └── screens/
```

## Build Configuration

### gradle.kts setup
```kotlin
plugins {
    id("com.google.dagger.hilt.android") version "2.48"
    kotlin("kapt")  // For annotation processing
}

dependencies {
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
}
```

### AndroidManifest.xml
```xml
<application
    android:name=".PaisaApp"  <!-- Must be declared -->
    ...>
</application>
```

## Troubleshooting

### "Cannot find Hilt binding"
- Make sure `@Inject` matches the provider in AppModule
- Check that the class is annotated with `@AndroidEntryPoint` or `@HiltViewModel`

### "Missing AbstractProcessor"
- Run clean build: `./gradlew clean build`
- Make sure `kapt` is in plugins

### ViewModel not injected
- Use `hiltViewModel()` in Compose
- Use `ViewModelProvider(this, defaultViewModelProviderFactory)` in activities

## References

- [Dagger Hilt Docs](https://dagger.dev/hilt/)
- [Android Hilt Guide](https://developer.android.com/training/dependency-injection/hilt-android)
- [Hilt Testing](https://developer.android.com/training/dependency-injection/hilt-testing)

---

This setup keeps the codebase clean, maintainable, and testable!
