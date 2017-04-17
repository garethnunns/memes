<?php
	require_once '../site/functions.php';
	check();
?><!DOCTYPE html>
<html>
	<head>
		<title>Memes · Add Meme</title>

		<?php include '../site/head.php'; ?>

		<script type="text/javascript" src="/site/dropzone.js"></script>

		<script type="text/javascript">
			Dropzone.options.memeUpload = {
				clickable: ".dz-message",
				autoProcessQueue: false,
				uploadMultiple: false,
				maxFilesize: 10,
				acceptedFiles: "image/jpg, image/jpeg, image/png",
				addRemoveLinks: true,
				init: function() {
					this.on("addedfile", function() {
						if (this.files[1]!=null) { // there are other images
							this.removeFile(this.files[0]); // delete them
						}
						document.getElementById("error").innerHTML = ''; // clear the error
					});

					var dz = this; // Makes sure that 'this' is understood inside the functions below.

					// for Dropzone to process the queue (instead of default form behavior):
					document.getElementById("post").addEventListener("click", function(e) {
						// Make sure that the form isn't actually being sent.
						e.preventDefault();
						e.stopPropagation();
						dz.processQueue();
					});

					//send all the form data along with the files:
					this.on("sending", function(data, xhr, formData) {
						formData.append("caption", document.getElementById("caption").value);
					});

					this.on("success",function(f, res) {
						try {
							var r = JSON.parse(res);

							if(r.success) {
								alert("returned: " + res); 
							}
							else {
								document.getElementById("error").innerHTML = (typeof r.error !== 'undefined') ? r.error : "There was an error";
							}
						}
						catch(e) { // this means there was probably a PHP error
							document.getElementById("error").innerHTML = "There was an internal error";
						}
					});
				}
			};
		</script>
	</head>

	<body>
		<?php include '../site/header.php'; ?>

		<div class="wrapper">
			<h1>Add a meme</h1>

			<form action="post.php" method="post" class="dropzone" enctype="multipart/form-data" id="memeUpload">
				<h3 class="dz-message">Drop your meme here (or click to upload)</h3>
				<p id="error" class="error"></p>
				<div class="fallback">
					<input name="file" type="file" />
				</div>
				<h3>Add a caption&hellip;</h3>
				<textarea name="caption" id="caption" placeholder="It's optional"></textarea>
				<p><input type="submit" id="post" value="Post"></p>
			</form>
		</div>

		<?php include '../site/footer.php'; ?>
	</body>
</html>