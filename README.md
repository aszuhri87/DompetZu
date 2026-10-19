# DompetZu - Documentation & Installation Guide

Dokumentasi resmi dan panduan instalasi lengkap untuk aplikasi manajemen keuangan **DompetZu** (Android Native dengan Jetpack Compose & Room Database).

---

## 1. Lingkungan Pengembangan & Versi Pemrograman

Berikut adalah spesifikasi teknologi dan versi bahasa pemrograman yang digunakan dalam aplikasi ini:

* **Bahasa Pemrograman**: Kotlin (`v2.2.10`)
* **Java Development Kit (JDK)**: Java 11 / Java 17 (Target Compatibility: `JavaVersion.VERSION_11`)
* **Framework UI**: Jetpack Compose (BOM `2024.09.00`) dengan Material Design 3
* **Build System**: Gradle dengan Android Gradle Plugin (AGP `v9.1.1`)
* **Symbol Processing**: KSP (Kotlin Symbol Processing `v2.3.5`)
* **Target Android SDK**: SDK 36 (Android 15 / Android 16)
* **Minimum Android SDK**: SDK 24 (Android 7.0 Nougat atau lebih baru)
* **IDE yang Direkomendasikan**: Android Studio (Ladybug / Jellyfish / Koala atau versi terbaru)

---

## 2. Konfigurasi Basis Data (Database)

Aplikasi ini menggunakan **Room Database** (abstraksi SQLite bawaan Android) untuk penyimpanan data lokal yang cepat, aman, dan dapat diakses tanpa koneksi internet (*offline-first*).

### A. Struktur & Arsitektur Database Lokal
* **Nama Database**: `app_database` (dikelola oleh `AppDatabase.kt`)
* **Lokasi Source Code**: `app/src/main/java/com/example/data/`
  * `database/AppDatabase.kt` : Inisialisasi basis data Room & Importer DAO.
  * `dao/` :
    * `TransactionDao.kt` (Manajemen transaksi pemasukan, pengeluaran & transfer)
    * `WalletDao.kt` (Manajemen akun/dompet & saldo)
    * `CategoryBudgetDao.kt` (Pelacakan batasan anggaran per kategori)
    * `BillDao.kt` (Daftar tagihan rutin & pengingat)
    * `SavingsGoalDao.kt` (Target tabungan uang & tabungan emas)

### B. Inisialisasi Database
Saat aplikasi dijalankan pertama kali, Room akan secara otomatis membuat berkas database SQLite di ruang penyimpanan internal aplikasi (*internal storage app path*). Tidak diperlukan instalasi server basis data eksternal (seperti MySQL atau PostgreSQL) untuk mode lokal ini.

### C. Konfigurasi Firebase Firestore (Opsional / Cloud Sync)
Aplikasi juga memiliki kesiapan integrasi cloud opsional menggunakan Firebase:
1. Masukkan file `google-services.json` pada direktori `/app/`.
2. Aktifkan konfigurasi di file `.env` (lihat bagian Variabel Lingkungan di bawah).

---

## 3. Cara Menjalankan Aplikasi di Lokal (Localhost / Emulator / Device)

### Langkah 1: Persiapan Project
1. Ekstrak file ZIP atau klon repositori proyek ini ke komputer Anda.
2. Pastikan Anda telah menginstal **Android Studio** beserta **Android SDK 36** dan **JDK 11 / 17**.

### Langkah 2: Membuka Proyek di Android Studio
1. Buka Android Studio.
2. Pilih **Open** / **Open an Existing Project**.
3. Arahkan ke folder proyek ini dan klik **OK**.
4. Tunggu Android Studio melakukan proses *Gradle Sync* dan mengunduh dependensi awal yang dibutuhkan.

### Langkah 3: Konfigurasi File Environment (`.env`)
1. Salin berkas `.env.example` menjadi `.env` di folder utama (root) proyek.
2. Jika menggunakan API eksternal (seperti Gemini API atau API Harga Emas), masukkan API Key Anda pada file `.env`:
   ```env
   GEMINI_API_KEY=your_api_key_here
   ```

### Langkah 4: Menjalankan Aplikasi di Emulator atau Perangkat Fisik
1. **Menggunakan Emulator (AVD)**:
   * Buka **Device Manager** di Android Studio.
   * Jalankan salah satu Android Virtual Device (misal: Pixel 8 / API 34 atau API 36).
   * Klik tombol **Run 'app'** (ikon segitiga hijau) di bagian toolbar Android Studio, atau tekan `Shift + F10`.
2. **Menggunakan Perangkat Fisik (Smartphone Android)**:
   * Aktifkan **USB Debugging** pada Smartphone Anda (*Developer Options*).
   * Hubungkan Smartphone ke PC via kabel USB.
   * Pilih nama smartphone Anda di daftar perangkat Android Studio, lalu klik **Run 'app'**.

### Langkah 5: Menjalankan Build via Command Line / Terminal (Opsional)
Anda juga dapat melakukan kompilasi dan menjalankan pengujian melalui terminal:
* **Kompilasi APK Debug**:
  ```bash
  gradle assembleDebug
  ```
  *Hasil APK dapat ditemukan di `app/build/outputs/apk/debug/app-debug.apk`*
* **Menjalankan Unit Test (Robolectric)**:
  ```bash
  gradle test
  ```

---

## 4. Rincian Fitur Utama Aplikasi

1. **Dashboard Arus Kas & Saldo Dompet**: Grafik rekap bulanan, kalkulasi kekayaan bersih (*Net Worth*), serta transfer antar-dompet.
2. **Anggaran Pintar & Peringatan**: Pengaturan limit pengeluaran per kategori dengan notifikasi visual.
3. **Pelacak Tagihan Rutin**: Jadwal pembayaran tagihan bulanan dengan indikator *status/stroke* adaptif.
4. **Target Tabungan Uang & Emas**: Kalkulator tabungan dan pemantauan harga acuan emas terkini.
5. **Mode Gelap & Terang**: Tampilan fleksibel dengan penyesuaian kontras tinggi di *Light Mode* maupun *Dark Mode*.

---

## 5. Bantuan & Dukungan
Jika Anda mengalami masalah saat instalasi atau kompilasi Gradle:
* Pastikan koneksi internet stabil saat proses *Gradle Sync* pertama kali.
* Lakukan **File > Invalidate Caches... > Invalidate and Restart** di Android Studio jika terjadi bentrokan *cache*.
