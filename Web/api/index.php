<?php
	require_once '../site/web.php';

	$current = $web.'api/0.1/';
	$current = '/api/0.1/';
?><!DOCTYPE html>
<html>
	<head>
		<title><?php echo "API · $sitename"; ?></title>

		<?php include '../site/head.php'; ?>
	</head>

	<body>
		<?php include '../site/header.php'; ?>

		<div class="wrapper">
			<h1><?php echo "$sitename API" ?></h1>
			<p>Welcome to our API. Below you can test your API calls, each has the expected fields for the request which are sent as post arguments to the page</p>

			<form method="POST" action="<?php echo $current; ?>login">
				<table class="api">
					<tr>
						<td colspan="2">
							<h2>Login</h2>
							<h4><?php echo $current; ?>login</h4>
						</td>
					</tr>
					<tr>
						<td>username</td>
						<td><input type="text" name="username"></td>
					</tr>

					<tr>
						<td>password</td>
						<td><input type="text" name="password"></td>
					</tr>

					<tr>
						<td colspan="2"><input type="submit" value="Send"></td>
					</tr>
				</table>
			</form>
		</div>

		<script type="text/javascript">
$("form").submit(function(e) {
	e.preventDefault();

	var form = this;

	$.post($(form).prop('action'), $(form).serialize(), function(data) {
		var ret = $('td.result', form).length ? $('td.result', this) : $('<tr><td colspan="2" class="result"></td></tr>').appendTo($('table.api',form)).children('td.result');
		ret.html('<h3>Returned:</h3>'+data);
	});
});
		</script>

		<?php include '../site/footer.php'; ?>
	</body>
</html>