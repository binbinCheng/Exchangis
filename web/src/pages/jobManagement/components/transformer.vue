<template>
  <div class="transformer-wrap">
    <!-- top -->
    <div class="tf-top">
      <span v-for="domain in dynamicValidateForm.domains" :key="domain.key">
        <span>校验：</span>
        {{ `${domain.optionVal} ${domain.value}` }}</span
      >
    </div>
    <!-- mid -->
    <div
      class="tf-mid"
      @click="showModal"
      :class="{ 'tf-mid-sync': !dynamicValidateForm.domains.length }"
    >
      <svg class="icon icon-symbol" aria-hidden="true">
        <use xlink:href="#icon-lansejiantoudaikuang"></use>
      </svg>
      <!--<img src="../../../images/jobDetail/u6239.png" />-->
      <!--<img-->
        <!--src="../../../images/jobDetail/u6240.png"-->
        <!--style="position: absolute; left: 72px; top: 6px; cursor: pointer"-->
      <!--/>-->
    </div>
    <!-- bottom -->
    <div class="tf-bottom">
      <span
        v-if="
          dynamicValidateForm.transf.value &&
          dynamicValidateForm.transf.param1 &&
          dynamicValidateForm.transf.param2 &&
          !dynamicValidateForm.transf.param3
        "
      >
        <span>转换：</span>
        {{
          `${dynamicValidateForm.transf.value}(${dynamicValidateForm.transf.param1},${dynamicValidateForm.transf.param2})`
        }}</span
      >
      <span
        v-if="
          dynamicValidateForm.transf.value &&
          dynamicValidateForm.transf.param1 &&
          dynamicValidateForm.transf.param2 &&
          dynamicValidateForm.transf.param3
        "
      >
        <span>转换：</span>
        {{
          `${dynamicValidateForm.transf.value}(${dynamicValidateForm.transf.param1},${dynamicValidateForm.transf.param2},${dynamicValidateForm.transf.param3})`
        }}</span
      >
    </div>

    <!-- 弹窗 -->
    <a-modal
      v-model:visible="visible"
      title="映射方式"
      @ok="handleOk"
      okText="保存"
    >
      <div class="tf-modal">
        <!-- top -->
        <div class="tf-modal-top">
          <div class="tf-modal-title">
            <span>校验函数</span>
          </div>
          <div class="tf-modal-content">
            <a-form ref="formRef" :model="dynamicValidateForm">
              <a-form-item
                v-for="domain in dynamicValidateForm.domains"
                :key="domain.key"
              >
                <div style="display: flex">
                  <a-select
                    ref="select"
                    v-model:value="domain.optionVal"
                    style="width: 98px"
                    :options="checkOptions"
                  >
                  </a-select>
                  <a-input
                    v-model:value="domain.value"
                    style="width: 144px; margin-left: 24px"
                  />
                  <MinusCircleOutlined
                    style="
                      font-size: 20px;
                      margin-left: 16px;
                      line-height: 32px;
                    "
                    class="dynamic-delete-button"
                    @click="removeDomain(domain)"
                  />
                </div>
              </a-form-item>
              <a-form-item v-if="!dynamicValidateForm.domains.length">
                <div style="display: flex; justify-content: center">
                  <a-button
                    type="dashed"
                    style="width: 220px"
                    @click="addDomain"
                  >
                    <PlusOutlined />
                    Add Check Funcion
                  </a-button>
                </div>
              </a-form-item>
            </a-form>
          </div>
        </div>
        <!-- bottom -->
        <div class="tf-modal-bottom">
          <div class="tf-modal-title">
            <span>转换函数</span>
          </div>
          <div class="tf-modal-content">
            <div>
              <a-select
                ref="select"
                v-model:value="dynamicValidateForm.transf.value"
                style="width: 120px"
                :options="transformFuncOptions"
                @change="changeDynamicValidateValue"
              >
              </a-select>
            </div>
            <div class="tf-modal-content-l">
              <span>(</span>
              <a-input
                v-model:value="dynamicValidateForm.transf.param1"
                :placeholder="placeholder1"
                style="width: 130px"
                size="small"
              />
              <!--<span style="line-height: initial">.</span>-->
              <a-input
                v-model:value="dynamicValidateForm.transf.param2"
                :placeholder="placeholder2"
                style="width: 130px"
                size="small"
              />
              <a-input
                v-if="dynamicValidateForm.transf.value && dynamicValidateForm.transf.value !== 'dx_substr'"
                v-model:value="dynamicValidateForm.transf.param3"
                :placeholder="placeholder3"
                style="width: 130px"
                size="small"
              />
              <span>)</span>
            </div>
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script>
import { defineComponent, ref, reactive, toRaw, watch, computed } from "vue";
import { message } from "ant-design-vue";
import {
  SyncOutlined,
  MinusCircleOutlined,
  PlusOutlined,
} from "@ant-design/icons-vue";
export default defineComponent({
  props: {
    tfData: Object,
  },
  emits: ["updateTransformer"],
  components: {
    SyncOutlined,
    MinusCircleOutlined,
    PlusOutlined,
  },
  setup(props, context) {
    let transformerMap = reactive({
      validator: props.tfData.validator || [],
      transformer: props.tfData.transformer || [],
      id: props.tfData.key,
      deleteEnable: props.tfData.deleteEnable
    });

    const newProps = computed(() => JSON.parse(JSON.stringify(props.tfData)));
    watch(newProps, (val, oldVal) => {
      const newVal = typeof val === "string" ? JSON.parse(val) : val;
      dynamicValidateForm.domains = transF(newVal.validator);
      dynamicValidateForm.transf = createTransformFunc(newVal.transformer);
    });

    const visible = ref(false);

    const showModal = () => {
      visible.value = true;
    };

    const handleOk = () => {
      const { domains, transf } = dynamicValidateForm;
      const transformer = {};
      const validator = [];
      domains.forEach((item) => {
        let str = item.optionVal + " " + item.value;
        validator.push(str);
      });

      if (dynamicValidateForm.transf.value) {
        if (!transf.param1 || !transf.param2) {
          return message.error('参数为必填')
        }
        const params = [];
        params.push(transf.param1);
        params.push(transf.param2);
        if (dynamicValidateForm.transf.value !== 'dx_substr') {
          if (!transf.param3) {
            return message.error('参数为必填')
          }
          params.push(transf.param3);
        }
        transformer.name = transf.value;
        transformer.params = params;
      }
      visible.value = false;
      context.emit("updateTransformer", {
        key: props.tfData.key,
        validator,
        transformer,
        deleteEnable: props.tfData.deleteEnable
      });
    };

    // 校验函数生成
    const transF = (arr) => {
      if (!arr) return [];
      const domains = [];
      arr.forEach((item, idx) => {
        let domain = Object.create(null);
        domain.key = idx + "";
        domain.optionVal = item.split(" ")[0];
        domain.value = item.split(" ").slice(-1).pop();
        domains.push(domain);
      });
      return domains;
    };

    // 转换函数生成
    const createTransformFunc = (transformer) => {
      if (!transformer) return {};
      let value = transformer.name && transformer.name;
      let param1 = transformer.params && transformer.params.length && transformer.params[0];
      let param2 = transformer.params && transformer.params.length > 1 && transformer.params[1];
      let param3 = transformer.params && transformer.params.length > 2 && transformer.params[2];
      return {
        value,
        param1,
        param2,
        param3
      };
    };

    const checkOptions = ref([
      {
        value: "like",
        label: "like",
      },
      {
        value: "not like",
        label: "not like",
      },
      {
        value: ">",
        label: "大于",
      },
      {
        value: "==",
        label: "等于",
      },
      {
        value: "<",
        label: "小于",
      },
      {
        value: ">=",
        label: "大于等于",
      },
      {
        value: "<=",
        label: "小于等于",
      },
    ]);

    const transformFuncOptions = ref([
      {
        value: "",
        label: "--"
      },
      {
        value: "dx_substr",
        label: "dx_substr",
      },
      {
        value: "dx_pad",
        label: "dx_pad",
      },
      {
        value: "dx_replace",
        label: "dx_replace",
      },
    ]);

    const formRef = ref();

    let dynamicValidateForm = reactive({
      domains: transF(transformerMap.validator),
      transf: createTransformFunc(transformerMap.transformer),
    });

    const removeDomain = (item) => {
      let index = dynamicValidateForm.domains.indexOf(item);

      if (index !== -1) {
        dynamicValidateForm.domains.splice(index, 1);
      }
    };

    const addDomain = () => {
      let key = dynamicValidateForm.domains.length + "";
      dynamicValidateForm.domains.push({
        value: "",
        optionVal: "like",
        key,
      });
    }

    let placeholder1 = ref(''),
      placeholder2 = ref(''),
      placeholder3 = ref('')

    const changeDynamicValidateValue = () => {
      dynamicValidateForm.transf.param1 = ''
      dynamicValidateForm.transf.param2 = ''
      dynamicValidateForm.transf.param3 = ''
      switch (dynamicValidateForm.transf.value) {
        case 'dx_substr':
          placeholder1.value = 'startIndex'
          placeholder2.value = 'length'
          placeholder3.value = ''
          break;
        case 'dx_pad':
          placeholder1.value = 'padType(r or l)'
          placeholder2.value = 'length'
          placeholder3.value = 'padString'
          break;
        case 'dx_replace':
          placeholder1.value = 'startIndex'
          placeholder2.value = 'length'
          placeholder3.value = 'replaceString'
          break;
        default:
          placeholder1.value = ''
          placeholder2.value = ''
          placeholder3.value = ''
          break;
      }
    }

    return {
      visible,
      showModal,
      handleOk,
      transformFuncOptions,
      checkOptions,
      formRef,
      dynamicValidateForm,
      removeDomain,
      addDomain,
      changeDynamicValidateValue,
      placeholder1,
      placeholder2,
      placeholder3
    };
  },
});
</script>

<style lang="less" scoped>
@import "../../../common/content.less";
.tf-mid {
  text-align: center;
  position: relative;
  height: 16px;
}

.tf-mid-sync {
  /*margin-top: 10px;*/
}
.tf-modal-content {
  display: flex;
  flex-direction: row;
}
.tf-modal-content-l {
  margin-left: 20px;
  line-height: 32px;
  > span {
    margin: 0 10px;
  }
}
.tf-modal-content {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
}
.tf-modal-title {
  margin-bottom: 20px;
}
.tf-bottom {
  text-align: center;
  height: 22px;
  > span {
    font-size: 12px;
  }
}
.tf-top {
  text-align: center;
  height: 22px;
  > span {
    font-size: 12px;
  }
  :nth-of-type(2) {
    margin: 0;
  }
  :nth-of-type(1) {
    max-width: 0;
  }
}
.icon-symbol {
  font-size: 50px;
  position: absolute;
  left: 72px;
  cursor: pointer;
  top: -16px;
}
</style>
