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

			<p>In general, there will always be a <em>'success'</em> boolean returned. If it's not successful there will be a fairly user friendly <em>'error'</em> accosciated. If it is successful then they'll be an array of arrays returned as well.</p>

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
			'desc' => "This is for requesting an individual meme.<br>thumb &amp; full are only preferred sizes and different sizes may be returned.<br>The <em>'poster'</em> elements provide the same information as <a href='#user'>user</a>.",
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
		'user' => [
			'name' => 'User profile',
			'desc' => "This will provide a <em>'profile'</em> array on success with useful information about the user with the <strong>id</strong>",
			'fields' => [
				'key' => [
					'type' => 'string',
					'kind' => 'text'
				],
				'id' => [
					'type' => 'int',
				],
			],
		],
		'profile' => [
			'name' => 'Profile Feed',
			'desc' => "This is primarily designed for outputting a profile page for the user with the provided <strong>id</strong>.<br>
				The <em>'user'</em> array is the same as the <em>'profile'</em> array returned by <a href='#user'>user</a>.<br>
				The <em>'memes'</em> array is a series of 20 memes (in the style of <a href='#meme'>meme</a> posted by the user, in reverse chronological of when they posted them<br>
				There are also some useful <em>'stats'</em> returned",
			'fields' => [
				'key' => [
					'type' => 'string',
					'kind' => 'text'
				],
				'id' => [
					'type' => 'int',
				],
				'page' => [
					'type' => 'int',
					'default' => 0,
				],
				'thumb' => [
					'type' => 'int',
					'default' => 400,
				],
				'full' => [
					'type' => 'int',
					'default' => 1000,
				]
			],
		],
		'feed' => [
			'name' => 'Meme Feed',
			'desc' => "This is a series of 20 memes (in the style of <a href='#meme'>meme</a> with the comments limited) which are in reverse chronological order of posting from the accounts the user follows",
			'fields' => [
				'key' => [
					'type' => 'string',
					'kind' => 'text'
				],
				'page' => [
					'type' => 'int',
					'default' => 0,
				],
				'thumb' => [
					'type' => 'int',
					'default' => 400,
				],
				'full' => [
					'type' => 'int',
					'default' => 1000,
				]
			],
		],
		'starred' => [
			'name' => 'Starred Memes Feed',
			'desc' => "This is a series of 20 memes (in the style of <a href='#meme'>meme</a> with the comments limited) which are in reverse chronological order from when the user has starred them",
			'fields' => [
				'key' => [
					'type' => 'string',
					'kind' => 'text'
				],
				'page' => [
					'type' => 'int',
					'default' => 0,
				],
				'thumb' => [
					'type' => 'int',
					'default' => 400,
				],
				'full' => [
					'type' => 'int',
					'default' => 1000,
				]
			],
		],
		'hot' => [
			'name' => 'Hot Memes Feed',
			'desc' => "This is a series of 20 memes (in the style of <a href='#meme'>meme</a> with the comments limited) which are in 'hotest' order - so the most interacted with and most suitable for the user first",
			'fields' => [
				'key' => [
					'type' => 'string',
					'kind' => 'text'
				],
				'page' => [
					'type' => 'int',
					'default' => 0,
				],
				'thumb' => [
					'type' => 'int',
					'default' => 400,
				],
				'full' => [
					'type' => 'int',
					'default' => 1000,
				]
			],
		],
		'stars' => [
			'name' => 'Stars List',
			'desc' => "Returns a list of 300 <em>'stars'</em> which are the stars on a meme with <strong>id</strong>.<br>
				Each has a <em>'user'</em> in the same format as the <em>'profile'</em> array returned from <a href='#user'>user</a>.",
			'fields' => [
				'key' => [
					'type' => 'string',
					'kind' => 'text'
				],
				'id' => [
					'type' => 'int',
				],
				'page' => [
					'type' => 'int',
					'default' => 0,
				],
			],
		],
		'reposts' => [
			'name' => 'Reposts List',
			'desc' => "Returns a list of 300 <em>'reposts'</em> which are the reposts on a meme with <strong>id</strong>.<br>
				Each has a <em>'user'</em> in the same format as the <em>'profile'</em> array returned from <a href='#user'>user</a>.",
			'fields' => [
				'key' => [
					'type' => 'string',
					'kind' => 'text'
				],
				'id' => [
					'type' => 'int',
				],
				'page' => [
					'type' => 'int',
					'default' => 0,
				],
			],
		],
		'followers' => [
			'name' => 'Followers List',
			'desc' => "Returns a list of 300 <em>'followers'</em> which follow the user with <strong>id</strong>.<br>
				Each has a <em>'user'</em> in the same format as the <em>'profile'</em> array returned from <a href='#user'>user</a>.",
			'fields' => [
				'key' => [
					'type' => 'string',
					'kind' => 'text'
				],
				'id' => [
					'type' => 'int',
				],
				'page' => [
					'type' => 'int',
					'default' => 0,
				],
			],
		],
		'following' => [
			'name' => 'Following List',
			'desc' => "Returns a list of 300 <em>'following'</em> which are followed by the user with <strong>id</strong>.<br>
				Each has a <em>'user'</em> in the same format as the <em>'profile'</em> array returned from <a href='#user'>user</a>.",
			'fields' => [
				'key' => [
					'type' => 'string',
					'kind' => 'text'
				],
				'id' => [
					'type' => 'int',
				],
				'page' => [
					'type' => 'int',
					'default' => 0,
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
	</body>
</html>