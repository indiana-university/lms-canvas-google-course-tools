# ess-lms-canvas-googleCourseTools

Requires a secret called `gctprops` with the following values:

```
gct.<env>.pickerApiKey=
gct.<env>.pickerClientId=
```
Where `<env>` represents an environment label, like `prd`, `stg`, `dev`, etc.

To Debug w/ Intellij, forward 5005 (in kube-forwarder, or k9s) to any desired port and then hook intellij up to that

```
helm upgrade googlecoursetools harbor-prd/k8s-boot -f helm-common.yaml -f helm-dev.yaml --install
```

```
helm upgrade googlecoursetools harbor-prd/k8s-boot -f helm-common.yaml -f helm-snd.yaml --install
```

```
helm upgrade googlecoursetools-rostersync ../k8s --values helm-common.yaml,helm-dev.yaml,helm-batch-rostersync.yaml --install
helm upgrade googlecoursetools-reg-rostersync ../k8s --values helm-common.yaml,helm-reg.yaml,helm-batch-rostersync.yaml --install -n ua-vpit--enterprise-systems--lms--helm-release
helm upgrade googlecoursetools-stg-rostersync ../k8s --values helm-common.yaml,helm-stg.yaml,helm-batch-rostersync.yaml --install -n ua-vpit--enterprise-systems--lms--helm-release
helm upgrade googlecoursetools-prd-rostersync ../k8s --values helm-common.yaml,helm-prd.yaml,helm-batch-rostersync.yaml --install -n ua-vpit--enterprise-systems--lms--helm-release

```