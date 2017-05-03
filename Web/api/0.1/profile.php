<?php
	require_once '../../site/functions.php';

	echo json_encode(profile($_POST['key'],$_POST['id'],$_POST['page'],$_POST['thumb'],$_POST['full']));
?>