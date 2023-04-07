<template>
	<div class="magic-task-info">
		<form>
			<label>{{ $i('message.enable') }}</label>
			<magic-checkbox v-model:value="info.enabled" />
			<label>cron</label>
			<magic-input v-model:value="info.cron" :placeholder="$i('task.form.placeholder.cron')" width="250px"/>
			<label>{{ $i('task.form.name') }}</label>
			<magic-input v-model:value="info.name" :placeholder="$i('task.form.placeholder.name')" width="250px"/>
			<label>{{ $i('task.form.path') }}</label>
			<magic-input v-model:value="info.path" :placeholder="$i('task.form.placeholder.path')" width="auto" style="flex:1"/>
		</form>
		<div style="flex:1;padding-top:5px;">
			<magic-textarea v-model:value="info.description" :placeholder="$i('task.form.placeholder.description')"/>
		</div>
	</div>
</template>
<script setup>
import { inject } from 'vue'
const $i = inject('i18n.format')
const info = inject('info')
</script>
<style scoped>
.magic-task-info{
	display: flex;
	flex-direction: column;
	flex: 1;
	padding: 5px;
}
.magic-task-info form{
	display: flex;
}
.magic-task-info form label{
	display: inline-block;
	width: 75px;
	height: var(--magic-input-height);
	line-height: var(--magic-input-height);
	font-weight: 400;
	text-align: right;
	padding: 0 5px;
}
.magic-task-info form :deep(.magic-checkbox){
	width: 22px;
	height: 22px;
}
.magic-task-info form :deep(.magic-textarea){
	margin: 5px;
}
</style>