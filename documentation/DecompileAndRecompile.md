# Decompile and recompile the application.

-	Decompile the apk to get the smali files to modify.
`apktool d MovieRatingApp.apk`

Open the folder `MovieRatingApp\smali_classes7\com\example\movierating\ui\screens`

The 2 screens to edit: MovieDetailScreen and MovieListItem

-	MovieDetailScreenKt$MovieDetailScreen$5
Search for getPosterURL and replace `move-result-object v54` with `const-string v54, "https://i.postimg.cc/HkzCnxf4/spiderman.jpg"` this will not store the result of getPoster to variable v54 but replace the poster with the custom one.

-	MovieListScreenKt$MovieListItem$1.smali replace `move-result-object v32` with `const-string v32, "https://i.postimg.cc/HkzCnxf4/spiderman.jpg"`

-	Build the apk back again with the changed smali code
`apktool b MovieRatingApp -o MovieRatingAppUA.apk`

-	Align the apk
`zipalign -p -f 4 MovieRatingAppUA.apk MovieRatingAppInjected.apk`

-	Not needed in demo (already done) but generate a signing key
`keytool -genkey -v -keystore keymonica -alias keymobile -keyalg RSA -keysize 2048 -validity 10000`

-	Sign the apk
`apksigner sign --ks keymonica --v1-signing-enabled true --v2-signing-enabled true MovieRatingAppInjected.apk`

-	uninstall the app on the emulator 

-	Install the injected apk
`adb install .\ MovieRatingAppInjected.apk`

Run the application and notice the changed images.