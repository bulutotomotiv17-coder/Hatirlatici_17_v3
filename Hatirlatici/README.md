# Hatırlatıcı (Android)

Renkli notlar + zamanı gelince hatırlatma. Tek ekranda tüm notlar, yalnızca telefonda çalışır (internet/hesap yok).

## Kurulum (APK üretip telefona yükleme)
1. Bilgisayara **Android Studio** kurun (ücretsiz): https://developer.android.com/studio
2. `Hatirlatici` klasörünü Android Studio ile açın (File > Open). İlk açılışta Gradle/SDK indirir (birkaç dakika).
3. Telefonda: Ayarlar > Telefon hakkında > "Yapı numarası"na 7 kez dokunun > Geliştirici seçenekleri > **USB hata ayıklama** açın.
4. Telefonu USB ile bağlayın, Android Studio'da ▶ **Run** düğmesine basın. Uygulama telefona kurulur.
   - APK dosyası istiyorsanız: Build > Build APK(s). Dosya `app/build/outputs/apk/debug/app-debug.apk` içinde çıkar; telefona kopyalayıp açabilirsiniz.

## İlk açılışta verilecek izinler
- **Bildirimler** (Android 13+)
- **Diğer uygulamaların üzerinde göster** – ekranın kilit açılınca ve alarm anında kendiliğinden belirmesi için şart
- **Tam ekran bildirim** (Android 14+)
- **Pil optimizasyonu dışı** – uygulama arka planda kapanmasın
Samsung/Xiaomi/Huawei/Oppo gibi telefonlarda ayrıca "Otomatik başlatma" izni verip uygulamayı "uyku moduna alınmayacak uygulamalar"a ekleyin.

## Nasıl çalışır
- **+** ile not ekleyin: metin, renk (10 renk), tarih-saat. Nota dokununca düzenlenir/silinir.
- Zamanı gelince: ekran uyanır, sarı-turuncu yanıp sönen hatırlatma ekranı açılır ve **tam iki kez bip** çalar (alarm ses düzeyinde).
- Ekran kilidini her açtığınızda, **TAMAM** demediğiniz hatırlatmalar yine ekranda belirir.
- TAMAM = tamamlandı, 10 DK ERTELE = 10 dakika sonra tekrar, ŞİMDİLİK KAPAT = bir sonraki kilit açışta tekrar gösterir.
- Telefon yeniden başlasa da hatırlatmalar korunur.
- Durum çubuğunda küçük bir "çalışıyor" bildirimi görünür; kilit açılışını dinlemek için Android bunu zorunlu kılar.

## GitHub ile APK üretme (Android Studio gerekmez)
1. github.com'da yeni (isterseniz Private) bir depo oluşturun.
2. Bu klasörün içeriğini depoya yükleyin (web'de "uploading an existing file" ile sürükle-bırak; `.github` klasörü dahil).
3. Depoda **Actions** sekmesi > son çalışma > en altta **Artifacts** > `Hatirlatici-apk` indirin (zip içinde app-debug.apk).
4. APK'yı telefona kopyalayıp açın ("bilinmeyen kaynaklardan yükleme"ye izin verin).
