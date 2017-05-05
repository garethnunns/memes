<?php
	require_once '../site/web.php';
	check();
?><!DOCTYPE html>
<html>
	<head>
		<title><?php echo 'Settings · ' . $sitename; ?></title>

		<?php include '../site/head.php'; ?>
	</head>

	<body>
		<?php include '../site/header.php'; ?>

		<div class="wrapper">
			<table class="settings">
				<tr>
					<td colspan="2"><h1>Your settings</h1></td>
				</tr>

<?php
	// reusing the query from the header
	$fields =[
		'FirstName' => [
			'name' => 'First Name',
			'current' => $header['profile']['firstName'],
			'type' => 'text',
			'min' => 1,
			'max' => 20,
			'pattern' => "^[a-zA-Z\s'\.-]{1,20}$",
			'title' => "The first name can only contain letters, spaces, apostrophes, hyphens and full stops",
		],
		'Surname' => [
			'name' => 'Surname',
			'current' => $header['profile']['surname'],
			'type' => 'text',
			'min' => 1,
			'max' => 20,
			'pattern' => "^[a-zA-Z\s'\.-]{1,20}$",
			'title' => "The surname can only contain letters, spaces, apostrophes, hyphens and full stops",
		],
		'Password' => [
			'name' => 'Password',
			'current' => '',
			'type' => 'password',
			'min' => 8,
			'max' => 50,
			'pattern' => "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])[a-zA-Z0-9]{8,50}$",
			'title' => "The password must contain a lowercase letter, an uppercase letter and a number",
		],
	];

	foreach ($fields as $uri => $field)
		echo "
		<tr>
			<td>{$field['name']}</td>
			<td>
				<form method='post' action='/ajax/user{$uri}.php'>
					<input type='{$field['type']}' value=\"{$field['current']}\" name='text' minlength='{$field['min']}' minlength='{$field['max']}' pattern=\"{$field['pattern']}\" title='{$field['title']}' placeholder='{$field['name']}'>
					<input type='submit' value='Update'>
				</form>
			</td>
		</tr>";

	echo "
	<tr>
		<td>Profile picture</td>
		<td>
			<form method='post' action='/ajax/userPicture.php' enctype='multipart/form-data'>
				<input type='file' name='file'>
				<input type='submit' value='Update'>
				<p><em>Note: your profile picture may take up to a day to change after updating it</em></p>
				<p class='center'><strong>Current:</strong><br>
				<img src='{$header['profile']['pic']}' alt='Your profile picture' id='current'></p>
			</form>
		</td>
	</tr>";
?>
			</table>

			<script type="text/javascript">
$("form").submit(function(e) {
	e.preventDefault();

	var form = this;

	// hide any existing errors
	$('.error',form).hide();

	var fdata = new FormData(form);

	var sub = $('[type="submit"]',form).val('Updating...');

	$.ajax({
		url: $(form).prop('action'),
		method: "POST",
		dataType: 'json',
		data: fdata,
		processData: false,
		contentType: false,
		success: function(data) {
			if(!data['success']) {
				var error = data['error'] ? data['error'] : 'There was an error whilst updating, please try again';
				$('<p class="error">'+error+'</p>').hide().appendTo(form).slideDown(250).delay(5000).slideUp(500);
				sub.val('Update');
			}
			else {
				if(typeof data['user']['pic'] !== 'undefined')
					$('.settings #current').attr('src',data['user']['pic']);
				sub.val('Updated!').delay(1000).queue(function(n) {
					sub.val('Update');
					n();
				});
				console.log(data);
			}
		},
		error: function(xhr) {
			$('<p class="error">There was a internal error whilst updating, please try again</p>').hide().appendTo(form).slideDown(250).delay(5000).slideUp(500);
			sub.val('Update');
		}
	}).always(function() {
		sub.blur();
	});
});
			</script>

		</div>

		<?php include '../site/footer.php'; ?>
	</body>
</html>