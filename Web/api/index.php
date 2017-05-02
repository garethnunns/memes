<?php
	require_once '../site/web.php';

	$current = '/api/0.1/';
	$currentAbs = $web.substr($current,1);
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

<?php
	$pages = [
		'login' => [
			'name' => 'Login',
			'desc' => "This will provide you with your user ID and key.<br>You then use this key to access the other elements of the API. The key may change periodically.",
			'fields' => [
				'username' => [
					'type' => 'string',
				],
				'password' => [
					'type' => 'string',
					'kind' => 'password'
				],
			],
		],
		'meme' => [
			'name' => 'Meme',
			'desc' => "This is for requesting an individual meme.<br>thumb &amp; full are only preferred sizes and different sizes may be returned",
			'fields' => [
				'key' => [
					'type' => 'string',
					'kind' => 'text'
				],
				'id' => [
					'type' => 'int',
				],
				'thumb' => [
					'type' => 'int',
					'default' => 400,
				],
				'full' => [
					'type' => 'int',
					'default' => 1000,
				],
				'limitComments' => [
					'type' => 'bool',
					'default' => true,
				],
			],
		],
	];

	echo "<table class='api'><tr><td colspan='2'><h2>Table of contents</h2><td></tr>";
	foreach ($pages as $uri => $page) 
		echo "<tr><td>{$page['name']}</td><td><a href='#{$uri}'>{$uri}</a>";
	echo "</table>";

	foreach ($pages as $uri => $page) { // output all the forms
		echo "
		<form method='POST' action='{$current}{$uri}' id='{$uri}'>
			<table class='api'>
				<tr>
					<td colspan='2'>
						<h2>{$page['name']}</h2>
						<h4>{$currentAbs}{$uri}</h4>
						<p>{$page['desc']}</p>
					</td>
				</tr>";
		
		foreach ($page['fields'] as $name => $field) {
			echo "
			<tr>
				<td>".(!isset($field['default']) ? "<strong>{$name}</strong>" : $name)." <em>[{$field['type']}]</em></td><td>";

			switch ($field['kind']) {
				case 'password':
					echo "<input type='password' name='{$name}'>";
					break;
				case 'text':
					echo "<textarea name='{$name}'></textarea>";
					break;
				default:
					echo "<input type='text' name='{$name}'>";
					break;
			}
			echo (isset($field['default']) ? " <em>(default: {$field['default']})</em>" : '')."</td>
			</tr>";
		}

		echo "	<tr>
					<td colspan='2'><input type='submit' value='Test'></td>
				</tr>
			</table>
		</form>
		";
	}
?>
		</div>

		<script type="text/javascript">
$("form").submit(function(e) {
	e.preventDefault();

	var form = this;

	var ret = $('tr.result', form).length ? $('tr.result', form) : $('<tr class="result"></tr>').appendTo($('table.api',form));

	$.post($(form).prop('action'), $(form).serialize(), function(data, s, xhr) {
		ret.html('<td>Returned ('+xhr.status+'):</td><td><code>'+JSON.stringify(data, null, 2).replace(/\n/g, "<br>").replace(/[ ]/g, "&nbsp;")+'</code></td>');
	},'json').fail(function(xhr) {
		ret.html('<td>Returned ('+xhr.status+'):</td><td>There was a server error completing your request</td>');
	});
});
		</script>

		<?php include '../site/footer.php'; ?>
	</body>
</html>